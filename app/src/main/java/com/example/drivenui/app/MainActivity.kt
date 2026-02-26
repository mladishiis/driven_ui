package com.example.drivenui.app

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.example.drivenui.app.presentation.openFile.view.OpenFileFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, OpenFileFragment.newInstance())
                .commit()
        }
    }
}