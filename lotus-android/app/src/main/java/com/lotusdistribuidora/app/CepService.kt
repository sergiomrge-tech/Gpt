package com.lotusdistribuidora.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class CepAddress(
    val street:String,
    val district:String,
    val city:String,
    val state:String,
    val complement:String
)

object CepService {
    suspend fun lookup(rawCep:String):CepAddress? = withContext(Dispatchers.IO) {
        val cep=rawCep.filter{it.isDigit()}
        if(cep.length!=8)return@withContext null
        var conn:HttpURLConnection?=null
        try{
            conn=(URL("https://viacep.com.br/ws/$cep/json/").openConnection() as HttpURLConnection).apply{
                requestMethod="GET"
                connectTimeout=6000
                readTimeout=6000
                setRequestProperty("Accept","application/json")
                setRequestProperty("User-Agent","Sistema-Lotus-Android")
            }
            if(conn.responseCode !in 200..299)return@withContext null
            val body=conn.inputStream.bufferedReader(Charsets.UTF_8).use{it.readText()}
            val o=JSONObject(body)
            if(o.optBoolean("erro",false))return@withContext null
            CepAddress(
                street=o.optString("logradouro"),
                district=o.optString("bairro"),
                city=o.optString("localidade"),
                state=o.optString("uf"),
                complement=o.optString("complemento")
            )
        }catch(_:Exception){
            null
        }finally{
            conn?.disconnect()
        }
    }
}
