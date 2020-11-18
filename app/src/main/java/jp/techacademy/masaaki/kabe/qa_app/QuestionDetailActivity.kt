package jp.techacademy.masaaki.kabe.qa_app

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ListView

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_question_detail.*
import java.lang.System.exit
import java.util.*
import kotlin.collections.ArrayList
import android.util.Base64

import kotlin.math.log

class QuestionDetailActivity : AppCompatActivity() {

    private lateinit var mQuestion: Question
    private lateinit var mAdapter: QuestionDetailListAdapter
    private lateinit var mAnswerRef: DatabaseReference
    private lateinit var mAuth:FirebaseAuth

    private val mEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>

            val answerUid = dataSnapshot.key ?: ""

            for (answer in mQuestion.answers) {

                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid == answer.answerUid) {
                    return
                }
            }

            val body = map["body"] ?: ""
            val name = map["name"] ?: ""
            val uid = map["uid"] ?: ""

            val answer = Answer(body, name, uid, answerUid)
            mQuestion.answers.add(answer)
            mAdapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {

        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_detail)



        // 渡ってきたQuestionのオブジェクトを保持する
        val extras = intent.extras
        mQuestion = extras.get("question") as Question

        //お気に入りボタンの表示非表示
        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {

            button1.visibility = View.GONE
        } else {
            button1.visibility = View.VISIBLE

        }

        if(user!=null) {
            //お気に入りに登録されているか判断
            val dataBaseReference = FirebaseDatabase.getInstance().reference
            val favoritesRef1 = dataBaseReference.child(favoritesPATH).child(user!!.uid)
            val questionUid = mQuestion.questionUid

            favoritesRef1.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val data = dataSnapshot.value as Map<String, String>?

                    if (data != null) {
                        val favorite_question_uid = data?.keys ?: "" as ArrayList<String>

                        for (i in favorite_question_uid) {
                            if (i == questionUid) {
                                button1.text = "お気に入りから削除"
                            }
                        }
                    } else {
                    }
                }

                override fun onCancelled(firebaseError: DatabaseError) {}
            })
        }

        title = mQuestion.title

        // ListViewの準備
        mAdapter = QuestionDetailListAdapter(this, mQuestion)
        listView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()




        fab.setOnClickListener {
            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser


            if (user == null) {
                // ログインしていなければログイン画面に遷移させる
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {
                // Questionを渡して回答作成画面を起動する
                // --- ここから ---
                val intent = Intent(applicationContext, AnswerSendActivity::class.java)
                intent.putExtra("question", mQuestion)
                startActivity(intent)
                // --- ここまで ---
            }
        }

        button1.setOnClickListener{v->

            val user = FirebaseAuth.getInstance().currentUser

            if (user != null) {
                if (button1.text == "お気に入りに追加") {
                    mAuth = FirebaseAuth.getInstance()
                    val user = mAuth.currentUser
                    val questionUid = mQuestion.questionUid

                    val data = HashMap<String, Int>()

                    data["genre"] = mQuestion.genre
                    //data["title"] = mQuestion.title
                    //data["body"] = mQuestion.body
                    //data["name"] = mQuestion.name
                    //data["uid"] = mQuestion.uid
                    //data["image"] = Base64.encodeToString(mQuestion.imageBytes, Base64.DEFAULT)


                    val dataBaseReference = FirebaseDatabase.getInstance().reference
                    val favoritesRef2 =
                        dataBaseReference.child(favoritesPATH).child(user!!.uid).child(questionUid)
                    favoritesRef2.setValue(data)
                    button1.text = "お気に入りから削除"

                } else {
                    val questionUid = mQuestion.questionUid
                    val dataBaseReference = FirebaseDatabase.getInstance().reference
                    val favoritesRef2 = dataBaseReference.child(favoritesPATH).child(user!!.uid).child(questionUid)
                    favoritesRef2.removeValue()
                    button1.text = "お気に入りに追加"
                }
            }
        }

        val dataBaseReference = FirebaseDatabase.getInstance().reference
        mAnswerRef = dataBaseReference.child(ContentsPATH).child(mQuestion.genre.toString()).child(mQuestion.questionUid).child(AnswersPATH)
        mAnswerRef.addChildEventListener(mEventListener)




    }
}