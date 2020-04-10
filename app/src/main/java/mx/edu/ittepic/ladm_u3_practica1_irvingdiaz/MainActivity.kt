package mx.edu.ittepic.ladm_u3_practica1_irvingdiaz

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {
    var imagenSeleccionada = 1
    var arregloIDLista = ArrayList<String>()
    var id_act_actual = 0
    var dia = ""
    var mes = ""
    var dialogoEvidencias:Dialog? = null
    var descripcion = ""
    @RequiresApi(Build.VERSION_CODES.O)
    var fechaEntrega = fecha()
    @RequiresApi(Build.VERSION_CODES.O)
    var fechaCaptura = fecha()
    var actividadSeleccionada = ""
    var bitmap :Bitmap ?= null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setTitle("Actividades - Irving Díaz")
        fechaActual.setText("Fecha de Captura: "+fecha())
        fechaEntregaV.setText("Fecha de Entrega: "+fecha())
        cargarLista()

        //changeListener para el calendario
        fechaSeleccion.setOnDateChangeListener { view, year, month, dayOfMonth ->
            //ajuste día
            if(dayOfMonth<=9){dia="0"+dayOfMonth}else{dia=""+dayOfMonth}

            //ajuste mes
            if((month+1)<=9){ mes= "0"+(month + 1) }else{mes=""+(month+1)}
            //formato fecha
            fechaEntrega = dia+"/" + mes + "/" + year

            //Actualizar fecha de entrega
            fechaEntregaV.setText("Fecha de Entrega: "+fechaEntrega)
        }

        //Listener del oboton agregar
        insertarActividad.setOnClickListener {
            if(descripcionCampo.text.isEmpty()){
                //verificar que no esté vacío el campo de descripción.
                mensaje("Agregar descripción, Campo vacío.")
                return@setOnClickListener
            }

            //Mostrar datos para confirmar la inserción de la actividad
            AlertDialog.Builder(this)
                .setTitle("Verificar información")
                .setMessage("Verificar que los datos de la actividad a insertar son los siguientes:\n\n" +
                        "Descripción/Nombre de actividad: \n${descripcionCampo.text.toString()}\n\n" +
                        "Fecha en el que se capturó:\n ${fechaCaptura}\n\n" +
                        "Fecha de entrega:\n ${fechaEntrega}\n")

                .setPositiveButton("Aceptar"){d,i->
                    insertar()
                    d.dismiss()
                }
                .setNegativeButton("Cancelar"){d,i->
                    d.cancel()
                }
                .show()
        }
    }
    //----------------------------------funciones ---------------------------------------------------
    @RequiresApi(Build.VERSION_CODES.O)
    fun insertar(){
        asignarDescripcion()
        var actividad =  Actividad ( descripcion , fechaCaptura , fechaEntrega )
        actividad.asignarPuntero(this)
        var resultado = actividad.insertar()
        if(resultado==true){
            mensaje("Se guardó la actividad correctamente")
            cargarLista()
            reiniciarDescripcion()
            //reiniciar las fechas
            fechaCaptura=fecha()
            fechaEntrega=fecha()
        }else{
            when(actividad.error){
                1->{ dialogo("Error en la tabla")
                }
                2->{ dialogo("Error al insertar")
                }
            }
        }
    }

    // consultando: https://www.programiz.com/kotlin-programming/examples/current-date-time
    @RequiresApi(Build.VERSION_CODES.O)
    fun fecha() : String {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val formatted = current.format(formatter)
        println("Current Date and Time is: $formatted")
        return formatted
    }

    fun mensaje(mensaje:String){
        Toast.makeText(this,mensaje, Toast.LENGTH_LONG).show()
    }

    fun dialogo(mensaje:String){
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Atención")
            .setMessage(mensaje)
            .setPositiveButton("ok"){d,i->}
            .show()
    }
    // Elegir foto
    // consulté : https://stackoverflow.com/questions/29803924/android-how-to-set-the-photo-selected-from-gallery-to-a-bitmap
    fun elegirFoto(){
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, imagenSeleccionada)
    }

    fun cargarLista(){
        try{
            var conexion = Actividad("","","")
            conexion.asignarPuntero(this)
            var data=conexion.mostrarActividades()
            if(data.size==0){
                if(conexion.error==3){
                    var vacio = Array<String>(data.size,{""})
                    lista.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,vacio)
                }
                return
            }
            var tamaño = data.size-1
            var vector = Array<String>(data.size,{""})
            arregloIDLista= ArrayList<String>()
            (0..tamaño).forEach{
                var actividad= data[it]
                var item = "\nDescripción / Nombre: \n"+actividad.descripcion+"\n"+
                        "\nFecha en que se capturó: \n"+actividad.fechaCaptura+"\n" +
                        "\nFecha de entrega: \n"+actividad.fechaEntrega+"\n"
                vector[it]=item
                arregloIDLista.add(actividad.id_actividad.toString())
            }
            lista.adapter= ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,vector)
            lista.setOnItemClickListener { parent, view, position, id ->

                var con = Actividad("","","")
                con.asignarPuntero(this)
                var actividadEncontrada = con.buscar(arregloIDLista[position])
                id_act_actual = actividadEncontrada.id_actividad

                if (con.error==4){
                    dialogo("Error no se contro ID")
                    return@setOnItemClickListener
                }

                actividadSeleccionada ="\nDescripción: "+actividadEncontrada.descripcion+"\n"+
                        "Fecha de captura: "+actividadEncontrada.fechaCaptura+ "\n" +
                        "Fecha de entrega: "+actividadEncontrada.fechaEntrega+"\n"

                AlertDialog.Builder(this)
                    .setTitle("Acciones para actividad")
                    .setMessage(actividadSeleccionada)
                    .setNegativeButton("Administrar Evidencias"){d,i->
                        administrarEvidencias()
                        d.dismiss()
                    }
                    .setPositiveButton("Eliminar Actividad"){d,i->
                        AlertDialog.Builder(this)
                            .setTitle("Cuidado!")
                            .setMessage("¿Estás seguro de eliminar esta actividad?")
                            .setPositiveButton("Aceptar"){d,i->
                                eliminarActividad(id_act_actual.toString())
                                cargarLista()
                                d.dismiss()
                            }
                            .setNegativeButton("Cancelar"){d,i->
                                d.cancel()
                            }.show()
                    }
                    .setNeutralButton("Cancelar"){d,i->
                        d.cancel()
                    }.show()
            }
        }catch (e:Exception){
            dialogo(e.message.toString())
        }
    }

    fun administrarEvidencias(){

        dialogoEvidencias = Dialog(this)
        dialogoEvidencias!!.setContentView(R.layout.dialogoevidencias)
        //botones
        var añadirEvidencia = dialogoEvidencias!!.findViewById<Button>(R.id.añadir)
        var regresar = dialogoEvidencias?.findViewById<Button>(R.id.regresar)
        var actividadsel = dialogoEvidencias?.findViewById<TextView>(R.id.actividadseleccionada)
        añadirEvidencia?.setOnClickListener {
            elegirFoto()
        }

        regresar?.setOnClickListener {
            dialogoEvidencias!!.cancel()
        }
        actividadsel?.setText(actividadSeleccionada)
        dialogoEvidencias?.show()
        mostrarFotos()
    }

    fun eliminarActividad(id:String){
        var conexion = Actividad("","","")
        conexion.asignarPuntero(this)
        var resultado = conexion.eliminar(id)
        if(resultado==true){
            mensaje("Se eliminó actividad")
        }else{
            when(conexion.error){
                1->{
                    dialogo("Error en tabla, no se creó o no se conectó a la base de datos")
                }
                2->{
                    dialogo("Error: No se pudo eliminar")
                } } } }

    fun mostrarFotos(){
        try{
            var conexion = Evidencia(0,byteArrayOf(0))
            conexion.asignarPuntero(this)
            var data = conexion.mostrarEvidencias(id_act_actual.toString())
            if(data.size==0){ return
            }
            var total = data.size-1
            var listadeEvidencias = dialogoEvidencias?.findViewById<LinearLayout>(R.id.fotos)
            (0..total).forEach{
                var evidencia= data[it]
                var img= ImageView(this)
                img.setImageBitmap(ConvertirByteArrayABitmap(evidencia))
                listadeEvidencias?.addView(img)
            }
        }catch (e:Exception){
            dialogo(e.message.toString())
        }
    }
   //consultando: http://ramsandroid4all.blogspot.com/2014/09/converting-byte-array-to-bitmap-in.html
    fun ConvertirByteArrayABitmap(byteArray: ByteArray?): Bitmap? {
        val arrayInputStream = ByteArrayInputStream(byteArray)
        var bitmap = BitmapFactory.decodeStream(arrayInputStream);
        return bitmap
    }

    //consultando: https://stackoverflow.com/questions/16177191/bitmap-compressbitmap-compressformat-png-0-fout-making-image-size-bigger-tha
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == imagenSeleccionada) {
            val imagenSeleccionadadata: Uri? = data?.data
            if(imagenSeleccionadadata==null){
                return}
            bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver,imagenSeleccionadadata)
            var listaEvidencias = dialogoEvidencias?.findViewById<LinearLayout>(R.id.fotos)
            val img= ImageView(this)
            img.setImageBitmap(bitmap)
            listaEvidencias?.addView(img)
            val bos = ByteArrayOutputStream()
            bitmap?.compress(Bitmap.CompressFormat.PNG, 98, bos)
            val bArray: ByteArray = bos.toByteArray()

            insertarTablaEvidencias(id_act_actual , bArray)
        }
    }

    fun insertarTablaEvidencias(id_a :Int,img:ByteArray){
        var evidencia = Evidencia(id_a,img)
        evidencia.asignarPuntero(this)
        var resultado = evidencia.insertar()
        if(resultado==true){
            mensaje("Se insertó la evidencia corrrectamente")
            cargarLista()
        }else{
            when(evidencia.error){
                1->{ dialogo("Error en tabla, no se creó o no se conectó a la base de datos")
                }
                2->{ dialogo("Error No se pudo insertar")
                } } } }

    fun asignarDescripcion(){
        descripcion = descripcionCampo.text.toString()
    }

    fun reiniciarDescripcion(){
        descripcionCampo.setText("")
    }
}
