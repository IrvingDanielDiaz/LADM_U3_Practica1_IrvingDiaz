package mx.edu.ittepic.ladm_u3_practica1_irvingdiaz

import android.app.Activity
import android.content.ContentValues
import android.database.sqlite.SQLiteException

class Actividad(desc:String, fechacap:String, fechaent:String) {
    //VALORES DE CADA ACTIVIDAD
    var id_actividad = 0
    var descripcion = desc
    var fechaCaptura = fechacap
    var fechaEntrega = fechaent
    var error = -1
    val nombreBaseDeDatos = "actividades"
    var puntero : Activity?= null

    fun asignarPuntero(p:MainActivity){
        puntero = p
    }

    fun insertar():Boolean{
        error=-1
        try{
            var base= SQLite(puntero!!,nombreBaseDeDatos,null,1)
            var insertar = base.writableDatabase
            var datos= ContentValues()

            datos.put("DESCRIPCION",descripcion)
            datos.put("FECHACAPTURA",fechaCaptura)
            datos.put("FECHAENTREGA",fechaEntrega)

            var respuesta =insertar.insert("ACTIVIDADES","ID_ACTIVIDAD",datos)
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

    fun mostrarActividades() : ArrayList<Actividad>{

        var data = ArrayList<Actividad>() //data que retorna la consulta
        error = -1 //reiniciar variable de error

        try {
            var base= SQLite(puntero!!,nombreBaseDeDatos,null,1)
            var select = base.readableDatabase
            var columnas = arrayOf("*")

            var cursor = select.query("ACTIVIDADES",columnas,null,null,null,null,null)

            if(cursor.moveToFirst()){
                do{
                    var actTemporal = Actividad(cursor.getString(1),cursor.getString(2),cursor.getString(3))
                    actTemporal.id_actividad = cursor.getInt(0)
                    data.add(actTemporal)
                }while (cursor.moveToNext())
            }else{
                error = 3
            }
        }catch (e: SQLiteException){
            error=1
        }
        return data
    }


    fun buscar(id:String): Actividad{
        var actEncontrada = Actividad("-1","-1","-1") //INICIALIZAR LA VARIABLE DE ACTIVIDAD
        error = -1 //REINICIO DE VARIABLE DE ERROR

        try {
            var base=SQLite(puntero!!,nombreBaseDeDatos,null,1)
            var select = base.readableDatabase
            var columnas = arrayOf("*")
            var idBuscar = arrayOf(id)
            var cursor = select.query("ACTIVIDADES", columnas ,"ID_ACTIVIDAD = ?",idBuscar,null,null,null)
            if(cursor.moveToFirst()){
                actEncontrada.id_actividad = id.toInt()
                actEncontrada.descripcion  = cursor.getString(1)
                actEncontrada.fechaCaptura = cursor.getString(2)
                actEncontrada.fechaEntrega = cursor.getString(3)
            }else{
                error=4
            }

        }catch (e: SQLiteException){
            error=1
        }
        return actEncontrada
    }


    fun eliminar(id:String):Boolean{
        error=-1
        try{
            var base= SQLite(puntero!!,nombreBaseDeDatos,null,1)
            var eliminar = base.writableDatabase
            var idBuscar = arrayOf(id)
            //primero eliminar las evidencias para poder eliminar las actividades
            var respuestaEvidencias =eliminar.delete("EVIDENCIAS","ID_ACTIVIDAD = ?",idBuscar)
            //Eliminar la actividad
            var respuestaActividades =eliminar.delete("ACTIVIDADES","ID_ACTIVIDAD = ?",idBuscar)

            if(respuestaEvidencias==-1){
                if(respuestaActividades == -1){
                    error=6
                    return false
                }
            }
        }catch (e: SQLiteException){
            error = 1
            return false
        }
        return true
    }
}