package com.example.fiatkyms.activity


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView

class ScanBarcodeActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {
    private lateinit var mScannerView: ZXingScannerView

    companion object {
        const val EXTRA_RESULT = "extra_result"
    }

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        mScannerView = ZXingScannerView(this)
        setContentView(mScannerView)
    }

    override fun onResume() {
        super.onResume()
        mScannerView.setResultHandler(this)
        mScannerView.startCamera()
    }

    override fun onPause() {
        super.onPause()
        mScannerView.stopCamera()
    }

    override fun handleResult(rawResult: Result) {
        val resultIntent = Intent()
        resultIntent.putExtra(EXTRA_RESULT, rawResult.text)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}