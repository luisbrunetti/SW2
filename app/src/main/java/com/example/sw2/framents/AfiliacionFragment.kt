package com.example.sw2.framents

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sw2.Clases.Afiliado
import com.example.sw2.Clases.Servicio_profile_afiliacion
import com.example.sw2.patrones_diseño.singleton.FirebaseConexion
import com.example.sw2.Clases.Usuario
import com.example.sw2.R
import com.example.sw2.Secundarios.Detalles_activity
import com.example.sw2.framents.secundarios.ReclycleViewAdapter_AfiliadoFragment
import com.example.sw2.interfaces.toolbar_transaction
import com.example.sw2.interfaces.translate_fragment
import com.example.sw2.interfaces.IntefaceClickListeer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.InternalCoroutinesApi

class AfiliacionFragment: Fragment(),
    IntefaceClickListeer {
    //Inicialización de vista
    private lateinit var vista : View
    //varaibless interfaces
    private var variable_cambio_fragment: translate_fragment? = null
    private var int_toolbar_trans_afiliacionfrag:toolbar_transaction? = null
    //Widgets del xml
    private var vista_value :String? = null
    private var botonSubir: Button? = null
    private var TextViewTittle:TextView? = null
    private var Toolbar_AfiliacionFragment: Toolbar ? = null
    private var tvi_enterprisename_porifle: TextView? = null
    private var tvi_email_profile:TextView? = null
    private var tvi_cant_servicios_value: TextView? =null
    private var tvi_dealsdone_profile:TextView? = null
    private var tvi_distrito_local_value:TextView? = null
    private var tvi_distrito_local:TextView? = null
    private var tvi_phone_local_value:TextView? = null
    private var iv_circle_view_profile_afiliado:ImageView? = null
    private var rv_afiliación_fragmentCustom:ReclycleViewAdapter_AfiliadoFragment? = null
    private var rv_afiliación_fragment_service :RecyclerView? = null
    private var iv_addService: ImageButton? = null
    // Varaible para para conexión con Firebase
    private lateinit var authFirbase:FirebaseAuth
    private var FirebaseConexion: FirebaseConexion? = null
    private lateinit var FirebaseDatabase:FirebaseDatabase
    private var user: Usuario? = null
    private var afiliado:Afiliado? = null
    /////
    private var ListServiciosAfiliado: ArrayList<Servicio_profile_afiliacion>? = null
    companion object {
        val GALERY_INTENT = 1
        private val IMAGE_PICK_CODE: Int = 1000
        private lateinit var  ReclyceViewAdapter_Servicio_profile_afiliacion : Servicio_profile_afiliacion
    }
    @InternalCoroutinesApi
    override fun onAttach(context: Context) {
        super.onAttach(context)
        variable_cambio_fragment = activity as translate_fragment
        int_toolbar_trans_afiliacionfrag = activity as toolbar_transaction
        ////////////////////////////
        this.FirebaseConexion = com.example.sw2.patrones_diseño.singleton.FirebaseConexion.getinstance(requireContext())
        user = FirebaseConexion?.getStoreSaved()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        ///////////Inicialización de toolbar///////////////////////
        int_toolbar_trans_afiliacionfrag?.change_tittle("Afiliación")
        //Escogiendo que tipo de fragment se mostrara////
        vista = if (!user?.Afiliado!!){
            inflater.inflate(R.layout.fragment_noafiliacion,container,false)
        }else{
            inflater.inflate(R.layout.fragment_afiliacion,container,false)
        }

        if (user?.Afiliado!!){
            tvi_enterprisename_porifle = vista.findViewById(R.id.tvi_enterprisename_porifle)
            tvi_distrito_local_value = vista.findViewById(R.id.tvi_distrito_local_value)
            tvi_email_profile = vista.findViewById(R.id.tvi_email_profile)
            tvi_phone_local_value = vista.findViewById(R.id.tvi_phone_local_value)
            iv_addService = vista.findViewById(R.id.add_service_profile_fragment)
            iv_circle_view_profile_afiliado = vista.findViewById(R.id.circle_view_profile_afiliado)
            tvi_cant_servicios_value = vista.findViewById(R.id.tvi_cant_servicios_value)
            rv_afiliación_fragment_service = vista.findViewById(R.id.recycleview_afiliacion_fragment)
            iv_circle_view_profile_afiliado?.setOnClickListener {
                val intent = Intent(Intent.ACTION_PICK)
                Log.d("action pick", Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent, ProfileFragment.GALERY_INTENT)
            }
            ListServiciosAfiliado = ArrayList<Servicio_profile_afiliacion>()
            FirebaseDatabase = com.google.firebase.database.FirebaseDatabase.getInstance()
            //////////////////////////////////
            retrieveDataProfileAfiliado()
            getServiceAfliado()

            iv_addService?.setOnClickListener {
                Log.d("dewbug","asd")
                variable_cambio_fragment?.cambiar_fragment("RegistrarNewServiceFragment",afiliado)
            }


        }else{
            var button:Button? = vista.findViewById(R.id.button_afiliarse_noafiliado)
            TextViewTittle = vista.findViewById(R.id.textView_title_noafiliado)
            TextViewTittle?.text = "!"+ user!!.Nombre + " todavia no te afilias!"
            button?.setOnClickListener {
                variable_cambio_fragment?.cambiar_fragment("RegisterAfiliado",null)
            }

        }
        //*********************************************************\\

        ///////////////////////////////////////////////////////////////////////

        return vista
    }

    private fun getServiceAfliado(){
        val queryrecycleView :Query = FirebaseDatabase.reference.child("Servicios").orderByChild("ID_Afiliado").equalTo(user!!.IDAfiliado)
        queryrecycleView.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                for(service in p0.children){
                    Log.d("Nombre servicio",p0.child("nombreTrabaj").value.toString())
                    val nombre = service.child("nombreTrabaj").value.toString()
                    val uriimagenservicio = service.child("Uri").value.toString()
                    val duracion = service.child("duracion").value.toString()
                    val categoriaservicio = service.child("Categoria_servicio").value.toString()
                    val emailservicio = service.child("Email_servicio").value.toString()
                    val tipopersona = service.child("Tipo_persona").value.toString()
                    val costoservicio = service.child("cost_service").value.toString()
                    val telefono = service.child("telefono").value.toString()
                    val idafiliado = service.child("ID_Afiliado").value.toString()
                    val descripcion = service.child("description").value.toString()
                    val key = service.child("key").value.toString()
                    val distrito = service.child("distrito").value.toString()
                    val calificacion = service.child("calificacion").value.toString()
                    val objectservice = Servicio_profile_afiliacion(categoriaservicio, emailservicio, idafiliado, tipopersona, uriimagenservicio, costoservicio, descripcion, distrito, duracion, key, nombre, telefono,calificacion)
                    ListServiciosAfiliado?.add(objectservice)
                }
                rv_afiliación_fragmentCustom = ReclycleViewAdapter_AfiliadoFragment(requireContext(),ListServiciosAfiliado!!,this@AfiliacionFragment)
                rv_afiliación_fragment_service?.layoutManager = LinearLayoutManager(requireContext())
                rv_afiliación_fragment_service?.adapter = rv_afiliación_fragmentCustom



            }
        })
    }
    private fun retrieveDataProfileAfiliado(){
        Toast.makeText(requireContext(),user?.Email.toString(),Toast.LENGTH_SHORT).show()
        Log.d("IDafilaiado",user!!.IDAfiliado.toString())
        val query: Query =
            com.google.firebase.database.FirebaseDatabase.getInstance().reference.child("Afiliados").orderByChild("ID_Afiliado")
                .equalTo(user!!.IDAfiliado)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
            override fun onDataChange(p0: DataSnapshot) {
                for (p0 in p0.children) {
                    Log.d("Empresa_servicio",p0.child("Distrito_servicio").value.toString())
                    tvi_enterprisename_porifle?.text = p0.child("Empresa_servicio").value.toString()
                    tvi_distrito_local_value?.text = p0.child("Distrito_servicio").value.toString()
                    tvi_email_profile?.text = p0.child("Email_servicio").value.toString()
                    tvi_phone_local_value?.text = p0.child("Telefono_servicio").value.toString()
                    tvi_cant_servicios_value?.text = p0.child("cant_servicio").value.toString()
                    tvi_dealsdone_profile?.text = p0.child("contratos_realizados").value.toString()
                    Log.d("cant servicio",p0.child("cant_servicio").value.toString())
                    Glide.with(vista)
                        .load(p0.child("URI_Imagen_Serivcio").value.toString().toUri())
                        .fitCenter()
                        .centerCrop()
                        .into(iv_circle_view_profile_afiliado!!)
                    afiliado = Afiliado(p0.child("Categoria_servicio").value.toString(), p0.child("Distrito_servicio").value.toString(),
                        p0.child("Email_servicio").value.toString(),p0.child("Empresa_servicio").value.toString(),p0.child("ID_Afiliado").value.toString(),
                        p0.child("ID_Usuario_node").value.toString(),p0.child("RUC").value.toString(),p0.child("Telefono_servicio").value.toString(),
                        p0.child("Tipo de persona").value.toString(),p0.child("URI_Imagen_Serivcio").value.toString(),p0.child("cant_servicio").value.toString(),
                        p0.child("contratos_realizados").value.toString()
                    )
                    Log.d("afiliado", afiliado!!.Email_servicio)
                }
            }
        })
    }

    override fun onClickListener(pos: Int) {
        val ob = ListServiciosAfiliado?.get(pos)!!
        Toast.makeText(context, "clicked", Toast.LENGTH_SHORT).show()
        val intent = Intent(
            activity,
            Detalles_activity::class.java
        )
        intent.putExtra("xml", "1")
        intent.putExtra("uri", ListServiciosAfiliado?.get(pos)!!.Uri.toString())
        intent.putExtra("nombretrabajo", ListServiciosAfiliado?.get(pos)!!.nombreTrabajo.toString())
        intent.putExtra("distrito", ob.distrito)
        intent.putExtra("email", ob.Email_servicio)
        intent.putExtra("tipopersona", ob.Tipo_persona)
        intent.putExtra("calificacion", ob.calificacion)
        intent.putExtra("costo", ob.cost_service)
        intent.putExtra("categoria", ob.categoria_servicio)
        intent.putExtra("telefono", ob.telefono)
        intent.putExtra("descripcion", ob.description)
        intent.putExtra("duracion", ob.duracion)
        //intent.putExtra("nombreempresa", ob.em)
        intent.putExtra("key", ob.key)
        intent.putExtra("id_afiliado",ob.ID_Ailiado)
        startActivity(intent)

    }

}
