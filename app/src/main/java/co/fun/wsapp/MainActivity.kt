package co.`fun`.wsapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttp
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.internal.ws.WebSocketExtensions
import okio.ByteString
import java.nio.ByteBuffer

class MainActivity : AppCompatActivity() {
	
	var ws: WebSocket? = null
	private val chatBox: TextView by lazy { findViewById(R.id.chatBox) }
	
	private val hello: Button by lazy { findViewById(R.id.hello) }
	private val ifunny: Button by lazy { findViewById(R.id.ifunny) }
	private val podlodka: Button by lazy { findViewById(R.id.podlodka) }
	private val close: Button by lazy { findViewById(R.id.close) }
	private val image: ImageView by lazy { findViewById(R.id.image) }
	private val open: Button by lazy { findViewById(R.id.open) }
	
	val okHttpClient = OkHttpClient.Builder()
		.build()
	
	val webSocketListener = object : WebSocketListener() {
		
		override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
			val picture = Base64.decode(bytes.base64(), Base64.DEFAULT)
			val bitmap = BitmapFactory.decodeByteArray(picture, 0, picture.size)
			image.post {
				image.visibility = View.VISIBLE
				image.setImageBitmap(bitmap)
			}
		}
		
		override fun onMessage(webSocket: WebSocket, text: String) {
			Log.d("WebSocket", text)
			image.visibility = View.GONE
			postMessage(text, Color.BLACK)
		}
		
		override fun onOpen(webSocket: WebSocket, response: Response) {
			Log.d("WebSocket", response.message)
			ws = webSocket
			postMessage("Connected", Color.GREEN)
		}
		
		override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
			ws = null
			val text = "Closed: $reason Code $code"
			postMessage(text, Color.RED)
		}
		
		override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
			ws = null
			val text = "Closing: $reason Code $code"
			postMessage(text, Color.RED)
		}
		
		override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
			Log.d("WebSocket", t.toString())
			val text = "ERROR: $t\r\n $response"
			postMessage(text, Color.RED)
		}
	}
	
	private fun postMessage(text: String, color: Int) {
		chatBox.post {
			chatBox.append(SpannableString("$text\r\n").apply {
				setSpan(
					ForegroundColorSpan(color), 0, text.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE
				)
			})
		}
	}
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		open bindClick {
			okHttpClient.newWebSocket(
				Request.Builder()
					.url(
						"ws://10.0.2.2:8081"
					)
					.build(), webSocketListener
			)
		}
		
		hello bindClick "hello"
		ifunny bindClick "ifunny"
		podlodka bindClick "podlodka"
		close bindClick { this?.close(1001, "Badass") }
	}
	
	private infix fun Button.bindClick(text: String) {
		setOnClickListener { ws?.send(text) }
	}
	
	private infix fun Button.bindClick(code: WebSocket?.() -> Unit) {
		setOnClickListener { code(ws) }
	}
}