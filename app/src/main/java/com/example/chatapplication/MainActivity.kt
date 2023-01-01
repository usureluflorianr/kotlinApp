package com.example.chatapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.renderscript.Sampler.Value
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import org.json.JSONObject
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var userRecyclerView: RecyclerView
    private lateinit var userList: ArrayList<User>
    private lateinit var adapter: UserAdapter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference
    private lateinit var sendButton: ImageView
    private lateinit var messageBox: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().getReference()
        sendButton = findViewById(R.id.sentButton)
        messageBox = findViewById(R.id.messageBox)

        userList = ArrayList()
        adapter = UserAdapter(this, userList)

        userRecyclerView = findViewById(R.id.userRecyclerView)

        userRecyclerView.layoutManager = LinearLayoutManager(this)
        userRecyclerView.adapter = adapter

        mDbRef.child("user").addValueEventListener(object: ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                userList.clear()
                for(postSnapshot in snapshot.children) {
                    val currentUser = postSnapshot.getValue(User::class.java)

                    if (mAuth.currentUser?.uid != currentUser?.uid) {
                        userList.add(currentUser!!)
                    }
                }

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        val requestQueue by lazy {
            Volley.newRequestQueue(this)
        }

        fun makeApiCall(whatToAsk : String) {
            val url = "https://api.openai.com/v1/images/generations"
            val key = "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"

            val data = JSONObject().apply {
                put("prompt", "a green truck")
                put("n", 1)
                put("size", "1024x1024")
            }

            val request = object : JsonObjectRequest(
                Method.POST,
                url,
                data,
                Response.Listener { response ->
                    // Extract the URL from the response data
                    val imageUrl = response.getJSONArray("data").getJSONObject(0).getString("url")

                    Toast.makeText(this, imageUrl, Toast.LENGTH_SHORT).show()

                    // Send a GET request to the URL
                    val imageRequest = JsonObjectRequest(
                        Method.GET,
                        imageUrl,
                        null,
                        Response.Listener { imageResponse ->
                            // Save the image to a file
                            File("image.jpg").writeBytes(imageResponse.toString().toByteArray())
                        },
                        Response.ErrorListener { error ->
                            // Do something with the error
                        }
                    )
                    Volley.newRequestQueue(this).add(imageRequest)
                },
                Response.ErrorListener { error ->
                    Toast.makeText(this, "API failed", Toast.LENGTH_SHORT).show()
                }
            ) {
                override fun getHeaders(): MutableMap<String, String> {
                    return mutableMapOf("Content-Type" to "application/json", "Authorization" to "Bearer $key")
                }
            }

            val queue = Volley.newRequestQueue(this)
            queue.add(request)

        }


        sendButton.setOnClickListener {

            val command = "python path/to/python/file.py"
            Runtime.getRuntime().exec(command)
            makeApiCall(messageBox.text.toString())
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.logout) {

            mAuth.signOut()
            val intent = Intent(this@MainActivity, Login::class.java)
            finish()
            startActivity(intent)
            return true
        }

        return true
    }
}