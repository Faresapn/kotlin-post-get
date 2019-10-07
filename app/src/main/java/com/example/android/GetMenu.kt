package com.example.android


import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.SimpleAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_get_menu.view.*
import org.json.JSONObject
import org.json.JSONStringer
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class GetMenu : Fragment() {
    val menulist = arrayListOf(hashMapOf<String,String>())
    lateinit var v : View;
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_get_menu, container, false)
        prepare()
        return  v
    }
    private fun getMenu(jsonstring: String) {
        repopulateData(JSONObject(jsonstring))
        renderlistdata()
    }

    private fun renderlistdata() {
        val adapter = SimpleAdapter(
            context, menulist, android.R.layout.simple_list_item_1, arrayOf("name", "category"),
            intArrayOf(android.R.id.text1, android.R.id.text2)
        )
        v.list_view.adapter = adapter
        v.list_view.setOnItemClickListener { parent: AdapterView<*>, view: View, i: Int, l: Long ->
            val intent = Intent(context, DetailActivity::class.java)
            intent.putExtra("name", menulist.get(i)["name"])
            intent.putExtra("category", menulist.get(i)["category"])
            startActivity(intent)
        }
    }

    private fun repopulateData(jsonObject: JSONObject) {
            menulist.clear()
            val data = jsonObject.getJSONArray("data")
            for (i in 0 until data.length()){
            val obj = data.getJSONObject(i)
            val name = obj.getString("name")
            val category = obj.getString("category")
            menulist.add(hashMapOf("name" to name , "category" to category))
        }
    }

    private fun prepare() {
        val url = URL("http://35.172.178.112:9000/api/menu/v1")
        networkHelper(url,"GET",this::getMenu)
    }

    private fun networkHelper(url: URL, method: String, onComplete:(String) -> Unit,param:JSONObject = JSONObject()) {
        val con = url.openConnection() as HttpURLConnection
        con.requestMethod = method
        con.setRequestProperty("Content-Type","application/json")
        con.setRequestProperty("Authorization","Basic dGVmYS0yMDE5OnBkd25ncGlhLXI3NDc4dHEtNDdsdHNxNmstZmFsdHN0cDVrZg==")
        try {
            AsyncTask.execute {
                if(method != "GET"){
                    con.doOutput =true
                    val out = OutputStreamWriter(con.outputStream)
                    out.write(param.toString())
                    out.close()
                }
                    val  responsecode =con.responseCode
                    if ( responsecode in 200..299){
                        val  result = streamToString(con.inputStream)
                        activity?.runOnUiThread {
                            onComplete(result)
                        }
                    }else{
                        activity?.runOnUiThread {
                            showError(responsecode)
                        }

                    }
            }
        }catch (e: Throwable){
            e.printStackTrace()
        }finally {
            con.disconnect()
        }
    }

    private fun streamToString(inputStream: InputStream?): String {
        val bufferedRender = BufferedReader(InputStreamReader(inputStream))
        var result = ""
        for (line in bufferedRender.readLines()){
            result += line
        }
        inputStream?.close()
        return  result
    }

    private fun showError(responsecode: Int) {
        Toast.makeText(context,"error code${responsecode}",Toast.LENGTH_LONG).show()
    }



}
