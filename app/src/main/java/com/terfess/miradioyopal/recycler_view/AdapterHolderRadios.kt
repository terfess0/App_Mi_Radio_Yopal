package com.terfess.miradioyopal.recycler_view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.terfess.miradioyopal.R
import com.terfess.miradioyopal.actividades.PantallaPrincipal
import com.terfess.miradioyopal.databinding.PantPrincipalBinding

class AdapterHolderRadios(private val context: Context, private val itemList: List<DatoEmisora>, var clasePrincipal: PantallaPrincipal, var vista:View) :
    RecyclerView.Adapter<AdapterHolderRadios.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.formato_recycler_radios, parent, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        holder.bind(item, context)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nombreRadio: TextView = itemView.findViewById(R.id.nombreRadio)
        private val imgRadio = itemView.findViewById<ImageButton>(R.id.emisora)


        private val binding = PantPrincipalBinding.bind(vista)

        // Para acceder a la opción de Facebook dentro del layout inflado
        private val facebook = binding.facebookOpcion
        private val claseFunciones = clasePrincipal
        fun bind(item: DatoEmisora, contexto:Context) {
            claseFunciones.nombreRadio = item.nombre
            nombreRadio.text = item.nombre

            PantallaPrincipal.urlNoti = item.linkStream
            PantallaPrincipal.nombreRadioNoti = item.nombre
            // Carga la imagen utilizando Glide
            Glide.with(contexto)
                .load(item.foto)
                .apply(RequestOptions().placeholder(R.drawable.radio_desconocida))
                .into(imgRadio)

            imgRadio.setOnClickListener {// NINGUNA
                claseFunciones.url = item.linkStream
                PantallaPrincipal.nombreRadioNoti = item.nombre
                claseFunciones.textoDireccional("Estas escuchando: ${item.nombre}")
                claseFunciones.iniciarReproductor(item.linkStream)

                facebook.setOnClickListener {
                    val enlaceFacebook = item.linkFacebook
                    claseFunciones.abrirFacebook(enlaceFacebook)
                }
            }


        }
    }
}