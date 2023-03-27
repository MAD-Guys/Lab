package it.polito.mad.lab2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast

class EditProfileActivity : AppCompatActivity() {

    private lateinit var fullName: String
    private lateinit var nickname: String
    private lateinit var sex: String
    private var age: Int = 0
    private lateinit var bio: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.edit_profile_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.confirm_button) {
            this.finish()
            val toast: Toast = Toast.makeText(this, "Now you are in SHOW mode!", Toast.LENGTH_SHORT)
            toast.show()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString("fullName", fullName)
        outState.putString("nickName", nickname)
        outState.putString("sex", sex)
        outState.putInt("age", age)
        outState.putString("bio", bio)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        fullName = savedInstanceState.getString("fullName").toString()
        nickname = savedInstanceState.getString("nickName").toString()
        sex = savedInstanceState.getString("sex").toString()
        age = savedInstanceState.getInt("age")
        bio = savedInstanceState.getString("bio").toString()
    }

}