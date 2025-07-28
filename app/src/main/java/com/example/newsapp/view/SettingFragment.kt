package com.example.newsapp.view

import AdsManager
import PremiumRepository
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.newsapp.R

import com.example.newsapp.databinding.FragmentSettingBinding
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoController
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.play.integrity.internal.ad
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

/**
 * A simple [Fragment] subclass.
 * Use the [SettingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingFragment : Fragment() {
    private var _binding: FragmentSettingBinding? =null
    private val binding : FragmentSettingBinding get() = _binding!!
    private var nativeAd: NativeAd? = null
    private lateinit var adView: AdView
    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser

        //TODO:: username instead of their email

        updateUI(currentUser)
        binding.apply{

            btnSignInOut.setOnClickListener {
                if (currentUser != null){
                    //act as a sign out
                    firebaseAuth.signOut()
                    updateUI(null)
                    Toast.makeText(requireContext(),"You signed out successfully.",Toast.LENGTH_SHORT).show()
                }
                else{
                    // act as a sign in button
                    val action = SettingFragmentDirections.actionSettingFragmentToSignInFragment()
                    findNavController().navigate(action)
                }

            }


            btnNotification.setOnClickListener {
                val action = SettingFragmentDirections.actionSettingFragmentToNotificationFragment()
                findNavController().navigate(action)
            }



            btnChangeTheme.setOnClickListener {
                //change theme logic

                val action = SettingFragmentDirections.actionSettingFragmentToAppearanceFragment()
                findNavController().navigate(action)


                /*val currentMode = AppCompatDelegate.getDefaultNightMode()

                val newMode = if (currentMode == AppCompatDelegate.MODE_NIGHT_YES){
                    AppCompatDelegate.MODE_NIGHT_NO
                }else{
                    AppCompatDelegate.MODE_NIGHT_YES
                }

                AppCompatDelegate.setDefaultNightMode(newMode)
                */
            }


            /*btnAdd.setOnClickListener {
                val action = SettingFragmentDirections.actionSettingFragmentToAddsFragment()
                findNavController().navigate(action)
            }*/
        }


        //ads logic

        val premiumRepository = PremiumRepository(requireContext())
        val adsManager = AdsManager(premiumRepository)
        val adView = layoutInflater.inflate(R.layout.native_ad_layout, null) as NativeAdView

        adsManager.loadNativeAd(binding.nativeAdContainer, _binding, isAdded, requireContext(), adView )


        val buttonCloseAd = adView.findViewById<Button>(R.id.btnCloseAd)

        buttonCloseAd.setOnClickListener{
            adsManager.closeNativeAd(binding.nativeAdContainer, binding, isAdded, requireContext(), adView)
        }


    }



    fun updateUI(currentUser: FirebaseUser?){
        if (currentUser != null){
            binding.welcomeMessage.text = "Welcome ${currentUser.email}"
            binding.btnSignInOut.text = "Sign out"
        }else{
            binding.welcomeMessage.text = "Please Sign in"
            binding.btnSignInOut.text = "Sign In"
        }
    }




    override fun onDestroyView() {
        super.onDestroyView()
        nativeAd?.destroy()
        nativeAd = null

        _binding = null
    }

}