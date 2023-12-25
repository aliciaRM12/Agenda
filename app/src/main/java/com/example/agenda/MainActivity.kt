package com.example.agenda

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.example.agenda.databinding.ActivityMainBinding
import com.example.agenda.model.Persona
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.botonGuardar.setOnClickListener {
            val nombre = binding.editNombre.text.toString()
            val telefono = binding.editTelefono.text.toString()
            val persona = Persona(nombre,telefono)
            val nombreArchivo = binding.editArchivo.text.toString()

            lifecycleScope.launch (Dispatchers.IO){
                guardarDatos(nombre,telefono)
            }
            guardarArchivo(persona,nombreArchivo)
            Snackbar.make(binding.root,"Archivo guardado correctamente",Snackbar.LENGTH_SHORT).show()
        }
        binding.botonConsultar.setOnClickListener {
            val dataFlow: Flow<Persona> =dataStore.data.map { preferences ->
                Persona(
                    nombre = preferences[stringPreferencesKey("nombre")] ?:"nombre no encontrado",
                    telefono = preferences[stringPreferencesKey("telefono")] ?:"telefono no encontrado"
                )
            }
            lifecycleScope.launch (Dispatchers.IO){
                Log.v("data_store", dataFlow.first().toString())
            }
            val nombreArchivo = binding.editArchivo.text.toString()
            val lectura = mostrarArchivo(nombreArchivo)
            binding.textMostrar.text = lectura
        }
    }

    private fun mostrarArchivo(nombreArchivo: String): String {
        val archivo = File(filesDir,nombreArchivo)
        val leer = BufferedReader(FileReader(archivo))
        var json = leer.readLine()
        var contenido = ""
        try{
            while(json!=null){
                contenido= contenido+json
                json=leer.readLine()
            }
            leer.close()
        }catch(e: Exception){
            e.printStackTrace()
        }
        return contenido
    }

    private fun guardarArchivo(persona: Persona, nombreArchivo: String) {
        val archivo = File(filesDir,nombreArchivo)
        val json = JSONObject()
        json.put("nombre",persona.nombre)
        json.put("telefono",persona.telefono)
        try{
            val escribir = FileWriter(archivo,true)
            escribir.write(json.toString())
            escribir.close()
        }catch(e: Exception){
            e.printStackTrace()
        }
    }
    private suspend fun guardarDatos(nombre: String, telefono: String) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("nombre")] = nombre
            preferences[stringPreferencesKey("telefono")] = telefono
        }
    }
}