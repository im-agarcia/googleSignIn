package com.example.googleauth.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.googleauth.R
import com.example.googleauth.databinding.FragmentMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider


class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding

    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var firebaseAuth: FirebaseAuth

    //constants
    private companion object {
        private const val RC_SIGN_IN = 100
        private const val TAG = "GOOGLE_SIGN_IN_TAG"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //here goes the google sign in
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(context,googleSignInOptions)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        //google signin button, click to begin the signin
        binding.googleSignIn.setOnClickListener{
            //begin google signIn
            Log.d(TAG, "onCreate: begin Google SignIn")
            val intent = googleSignInClient.signInIntent
            startActivityForResult(intent, RC_SIGN_IN)

        }
    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if(firebaseUser != null){
            //user is already loggedin
            findNavController().navigate(R.id.action_mainFragment_to_profileFragment)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //result returned from launching the internet from GoogleSignInApi.getSignInIntent
        if(requestCode == RC_SIGN_IN){
            Log.d(TAG,"onActivityResult: Google SignIn intent result")
            val accountTask = GoogleSignIn.getSignedInAccountFromIntent(data)
            try{
                //google sign in success, now auth with firebase
                val account = accountTask.getResult(ApiException::class.java)
                firebaseAuthWhitGoogleAccount(account)
            }
            catch (e: Exception){
                //failed google sign in
                Log.d(TAG, "onActivityResult: ${e.message}")
            }
        }
    }

    private fun firebaseAuthWhitGoogleAccount(account: GoogleSignInAccount?) {
        Log.d(TAG, "firebaseAuthWithGoogleAccount: begin firebase auth with Google Account")

        val credential = GoogleAuthProvider.getCredential(account!!.idToken,null)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                //login success
                Log.d(TAG, "firebaseAuthWithGoogleAccount: LoggedIn")

                //get loggedIn user
                val firebaseUser = firebaseAuth.currentUser
                //get user info
                val uid = firebaseUser!!.uid
                val email = firebaseUser.email

                Log.d(TAG, "firebaseAuthWhitGoogleAccount: Uid: $uid")
                Log.d(TAG, "firebaseAuthWhitGoogleAccount: Email:$email")

                //check if user is new or existing
                if(authResult.additionalUserInfo!!.isNewUser){
                    //user is new > Account created
                    Log.d(TAG, "firebaseAuthWhitGoogleAccount: Account created... \n$email")
                    Toast.makeText(context, "Account created... \\n$email", Toast.LENGTH_SHORT).show()
                }
                else {
                    //existing user - LoggedIn
                    Log.d(TAG, "firebaseAuthWhitGoogleAccount: Existing user.. \n$email")
                    Toast.makeText(context, "LoggedIn.. \\n$email",Toast.LENGTH_SHORT).show()
                }
                //start profile fragment
                findNavController().navigate(R.id.action_mainFragment_to_profileFragment)

            }
            .addOnFailureListener{e ->
                //login failed
                Log.d(TAG, "firebaseAuthWhitGoogleAccount: Loggin Failed due to ${e.message}")
                Toast.makeText(context,"Loggin Failed due to ${e.message}", Toast.LENGTH_SHORT).show()

            }


    }

}