package mx.edu.ittepic.ladm_u3_practica1_irvingdiaz

import android.app.Activity
import android.content.ContentValues
import android.database.sqlite.SQLiteException


class Evidencia(idactividad:Int, fotoV:ByteArray) {
    var id_Evidencia=0
    var id_Actividad= idactividad
    var foto : ByteArray= fotoV
    val nombreBaseDeDatos = "actividades"
    var puntero : Activity?= null
    var error = -1
    fun asignarPuntero (p:MainActivity){
        puntero = p
    }

    fun insertar():Boolean{
        error=-1
        try{
            var base= SQLite(puntero!!,nombreBaseDeDatos,null,1)
            var insertar = base.writableDatabase
            var datos= ContentValues()
            datos.put("ID_ACTIVIDAD",id_Actividad)
            datos.put("FOTO",foto)

            var respuesta =insertar.insert("EVIDENCIAS","IDEVIDENCIA", datos)
            if(respuesta.toInt()==-1){
                error=2
                return false
            }

        }catch (e: SQLiteException){
            error=1
            return false
        }
        return true
    }

    fun mostrarEvidencias(id:String):ArrayList<ByteArray>{
        var data = ArrayList<ByteArray>()
        error = -1

        try {
            var base= SQLite(puntero!!, nombreBaseDeDatos ,null,1)
            var select = base.readableDatabase
            var columnas = arrayOf("*")
            var idBuscar = arrayOf(id)
            var cursor = select.query("EVIDENCIAS",columnas,"ID_ACTIVIDAD=?",idBuscar,null,null,null)
            if(cursor.moveToFirst()){
                do{
                    data.add(cursor.getBlob(2))
                }while (cursor.moveToNext())
            }else{
                error = 3
            }

        }catch (e: SQLiteException){
            error=1
        }

        return data
    }

    fun eliminar(id:String):Boolean{
        error=-1
        try{
            var base= SQLite(puntero!!,nombreBaseDeDatos,null,1)
            var eliminar = base.writableDatabase
            var idBuscar = arrayOf(id)
            var respuestaEvidencias =eliminar.delete("EVIDENCIAS","ID_ACTIVIDAD = ?",idBuscar)
            if(respuestaEvidencias==-1){
                    error=6
                    return false
            }
        }catch (e: SQLiteException){
            error = 1
            return false
        }
        return true
    }
}