package mx.edu.ittepic.ladm_u3_practica1_irvingdiaz

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import androidx.annotation.RequiresApi

class SQLite(
    context: Context?,
    name: String?,
    factory: SQLiteDatabase.CursorFactory?,
    version: Int
) : SQLiteOpenHelper(context, name, factory, version) {

    override fun onCreate(db : SQLiteDatabase?) {
        //TABLA ACTIVIDADES
        db?.execSQL("CREATE TABLE ACTIVIDADES(ID_ACTIVIDAD INTEGER PRIMARY KEY AUTOINCREMENT," +
                " DESCRIPCION VARCHAR(2000)," +
                "FECHACAPTURA DATE," +
                "FECHAENTREGA DATE)")
        //TABLA EVIDENCIAS
        db?.execSQL("CREATE TABLE EVIDENCIAS(IDEVIDENCIA INTEGER PRIMARY KEY AUTOINCREMENT," +
                " ID_ACTIVIDAD INTEGER NOT NULL," +
                " FOTO BLOB," +
                " FOREIGN KEY (ID_ACTIVIDAD) REFERENCES ACTIVIDADES(ID_ACTIVIDAD))") //LLAVE FORÁNEA
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}