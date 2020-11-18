package jp.techacademy.masaaki.kabe.qa_app

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.constraint.solver.widgets.Snapshot
import android.util.Base64
import android.util.Log
import android.widget.ListView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_question_detail.*

class favorites : AppCompatActivity() {

   // private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mListView: ListView
    private lateinit var mQuestionArrayList: ArrayList<Question>
    private lateinit var mAdapter: QuestionsListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        title="お気に入り質問一覧"


        // ListViewの準備
        mListView = findViewById(R.id.listView2)
        mAdapter = QuestionsListAdapter(this)
        mQuestionArrayList = ArrayList<Question>()
        mAdapter.setQuestionArrayList(mQuestionArrayList)
        mListView.adapter = mAdapter
        //mAdapter.notifyDataSetChanged()




        val mDatabaseReference = FirebaseDatabase.getInstance().reference
        val user = FirebaseAuth.getInstance().currentUser
        val favoritesRef = mDatabaseReference.child(favoritesPATH).child(user!!.uid)

        favoritesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val data = dataSnapshot.value as Map<String,Map<String,Int>>?


                if (data != null) {
                    val data2 = data.keys
                    val data3 = data.values

                    val genreArrayList = ArrayList<String>()
                    for (i in data3) {
                         val genre=  i.get("genre").toString()
                        genreArrayList.add(genre)
                    }

                    var a=-1
                    for(questionUid in data2) {
                        a=a+1
                        val Genre=genreArrayList[a]

                        //Log.d("kotlintest10",questionUid)
                        //Log.d("kotlintest10",Genre)


                        //ここから質問内容をゲット

                        val favoritesRef1 = mDatabaseReference.child(ContentsPATH).child(Genre.toString()).child(questionUid)
                        favoritesRef1.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {

                                val map = dataSnapshot.value as Map<String, String>


                                ////////////////
                                val mGenre=Genre.toInt()


                                val title = map["title"] ?: ""
                                Log.d("kotlintest10",title)

                                val body = map["body"] ?: ""
                                Log.d("kotlintest10",body)
                                val name = map["name"] ?: ""
                                val uid = map["uid"] ?: ""
                                val imageString = map["image"] ?: ""
                                val bytes =
                                    if (imageString.isNotEmpty()) {
                                        Base64.decode(imageString, Base64.DEFAULT)
                                    } else {
                                        byteArrayOf()
                                    }

                                val answerArrayList = ArrayList<Answer>()
                                val answerMap = map["answers"] as Map<String, String>?
                                if (answerMap != null) {
                                    for (key in answerMap.keys) {
                                        val temp = answerMap[key] as Map<String, String>
                                        val answerBody = temp["body"] ?: ""
                                        val answerName = temp["name"] ?: ""
                                        val answerUid = temp["uid"] ?: ""
                                        val answer = Answer(answerBody, answerName, answerUid, key)
                                        answerArrayList.add(answer)
                                    }
                                }

                                val question = Question(title, body, name, uid, dataSnapshot.key ?: "",
                                    mGenre, bytes, answerArrayList)



                                mQuestionArrayList.add(question)


                                mAdapter.notifyDataSetChanged()

                                //





                            }

                           override fun onCancelled(firebaseError: DatabaseError) {}
                        })
                        //ここまで

                    }//for


                   } else {
                }
            }

            override fun onCancelled(firebaseError: DatabaseError) {}
        })




        //mDatabaseReference = FirebaseDatabase.getInstance().reference






    }//oncreate
}












