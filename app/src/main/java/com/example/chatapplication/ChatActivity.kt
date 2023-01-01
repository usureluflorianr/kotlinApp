package com.example.chatapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import org.json.JSONObject

import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class ChatActivity : AppCompatActivity() {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageBox: EditText
    private lateinit var sendButton: ImageView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message>
    private lateinit var mDbRef: DatabaseReference

    var receiverRoom: String? = null
    var senderRoom: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val name = intent.getStringExtra("name")
        val receiverUid = intent.getStringExtra("uid")

        val senderUid = FirebaseAuth.getInstance().currentUser?.uid
        mDbRef = FirebaseDatabase.getInstance().getReference()

        senderRoom = receiverUid + senderUid
        receiverRoom = senderUid + receiverUid

        supportActionBar?.title = name

        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        messageBox = findViewById(R.id.messageBox)
        sendButton = findViewById(R.id.sentButton)

        messageList = ArrayList()
        messageAdapter = MessageAdapter(this, messageList)

        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = messageAdapter

        var toRespond = ""

        mDbRef.child("chats").child(senderRoom!!).child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    messageList.clear()

                    for(postSnapshot in snapshot.children) {

                        val message = postSnapshot.getValue(Message::class.java)

                        val sender = message?.senderId

                        if (sender != senderUid)
                        {
                            toRespond = toRespond + ". " + message?.message;
                        }
                        else
                        {
                            toRespond = ""
                        }

                        messageList.add(message!!)
                    }
                    messageAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

        val requestQueue by lazy {
            Volley.newRequestQueue(this)
        }

        fun makeApiCall(whatToAsk : String) {
            // Set the API endpoint URL
            val url = "https://api.openai.com/v1/completions"

            // Set the API key
            val apiKey = "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"

            // Set the request headers
            val headers = HashMap<String, String>()
            headers["Content-Type"] = "application/json"
            headers["Authorization"] = "Bearer $apiKey"

            // Set the request body
            val data = JSONObject()
            data.put("model", "text-davinci-003")
            data.put("prompt", whatToAsk)
            data.put("max_tokens", 1000)
            data.put("temperature", 0)

            val request = object : StringRequest(
                Method.POST, url,
                Response.Listener { response ->
                    // Parse the JSON response
                    val jsonResponse = JSONObject(response)

                    messageBox.setText(jsonResponse.optJSONArray("choices")?.optJSONObject(0)?.optString("text")?.trim().toString())
                            .toString()

                    val message = messageBox.text.toString()

                    val messageObject = Message(message, senderUid)

                    mDbRef.child("chats").child(senderRoom!!).child("messages").push()
                        .setValue(messageObject).addOnSuccessListener {
                            mDbRef.child("chats").child(receiverRoom!!).child("messages").push()
                                .setValue(messageObject)
                        }

                    messageBox.setText("")
                },
                Response.ErrorListener { error ->
                    // Handle the error
                    Toast.makeText(this, "API call failed", Toast.LENGTH_SHORT).show()
                }
            ) {
                // Set the request headers
                override fun getHeaders(): MutableMap<String, String> {
                    return headers
                }

                // Set the request body
                override fun getBody(): ByteArray {
                    return data.toString().toByteArray()
                }
            }

            requestQueue.add(request)
        }

        sendButton.setOnClickListener {

            if (messageBox.text.toString() == "auto")
                makeApiCall(toRespond.trim())

            else {
                Toast.makeText(this, toRespond, Toast.LENGTH_SHORT).show()

                val message = messageBox.text.toString()

                val messageObject = Message(message, senderUid)

                mDbRef.child("chats").child(senderRoom!!).child("messages").push()
                    .setValue(messageObject).addOnSuccessListener {
                        mDbRef.child("chats").child(receiverRoom!!).child("messages").push()
                            .setValue(messageObject)
                    }
                messageBox.setText("")
            }
        }
    }
}







