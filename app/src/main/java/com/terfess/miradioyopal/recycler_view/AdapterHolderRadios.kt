package com.terfess.miradioyopal.recycler_view

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.terfess.miradioyopal.R
import com.terfess.miradioyopal.actividades.HomeScreen
import com.terfess.miradioyopal.databinding.ActivityHomeScreenBinding

class AdapterHolderRadios(
    private val context: Context,
    private val itemList: List<DatoStationLocal>,
    var homeScreenInstance: HomeScreen,
    var vista: View
) :
    RecyclerView.Adapter<AdapterHolderRadios.ViewHolder>() {

    var radioFocus: ConstraintLayout? = null
    var lastClickTime: Long = 0


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(context)
                .inflate(R.layout.formato_recycler_radios, parent, false)

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
        private val debounceInterval =
            600L // Intervalo de tiempo mínimo entre clics en milisegundos

        private val binding = ActivityHomeScreenBinding.bind(vista)
        private val nameRadio = itemView.findViewById<TextView>(R.id.nameRadio)
        private val imgRadio = itemView.findViewById<ImageButton>(R.id.station)
        val cardRadio = itemView.findViewById<ConstraintLayout>(R.id.contain_radio)


        // go facebook option
        private val facebook = binding.facebookOpcion
        private val functions = homeScreenInstance
        //

        fun bind(item: DatoStationLocal, contexto: Context) {
            //set radio name
            nameRadio.text = item.name

            // Load image radio with glide
            Glide.with(contexto)
                .load(item.photo)
                .placeholder(R.drawable.radio_desconocida)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imgRadio)

            // On click Radio
            imgRadio.setOnClickListener {

                val currentTime = System.currentTimeMillis()
                if (currentTime - lastClickTime > debounceInterval) {
                    lastClickTime = currentTime

                    removeFocusRadio()
                    radioFocus = cardRadio
                    onSelectRadio()
                    println("Click valido")

                    binding.cargando.visibility = View.VISIBLE
                    binding.textoHorizontal.text = context.getString(R.string.buscando)
                    binding.play.visibility = View.GONE

                    //start sound player
                    functions.starPlayer(item)

                    //On click go facebook option
                    facebook.setOnClickListener {
                        val enlaceFacebook = item.linkFacebook
                        functions.abrirFacebook(enlaceFacebook)
                    }
                } else {

                    val snk = Snackbar.make(
                        binding.root,
                        "Calmate, toques muy rápidos!",
                        Snackbar.LENGTH_SHORT
                    )
                    snk.animationMode = BaseTransientBottomBar.ANIMATION_MODE_FADE
                    snk.setBackgroundTint(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.color_primario
                        )
                    )
                    snk.show()
                    println("Click no valido")
                }
            }

            HomeScreen.SharedData.valueClose.observe(homeScreenInstance.contextActivity, Observer {
                if (it) removeFocusRadio()
            })
        }

        private fun onSelectRadio() {
            radioFocus?.let { cardRadio ->
                val borderWidth = 8 // Ancho del borde en píxeles
                val selectedBorderColor =
                    ContextCompat.getColor(binding.root.context, R.color.colorBorde)

                // Crear y asignar el drawable solo si es necesario
                val selectedDrawable = GradientDrawable().apply {
                    setStroke(borderWidth, selectedBorderColor)
                    cornerRadius = 16f
                }

                cardRadio.background = selectedDrawable
                println("is selected: ")
            }
        }

        private fun removeFocusRadio() {
            radioFocus?.let { cardRadio ->
                // Crear y asignar el drawable solo si es necesario
                val selectedDrawable = GradientDrawable().apply {
                    setStroke(0, null)
                }

                cardRadio.background = selectedDrawable
                println("is remove focus radio: ")
            }
        }

    }
}