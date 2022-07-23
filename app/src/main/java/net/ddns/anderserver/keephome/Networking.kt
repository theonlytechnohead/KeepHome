package net.ddns.anderserver.keephome

import com.android.volley.Response.ErrorListener
import com.android.volley.Response.Listener
import com.android.volley.toolbox.StringRequest

class Networking {

    companion object {
        fun constructPOST(
            address: String,
            parameters: MutableMap<String, String>,
            response: Listener<String>,
            error: ErrorListener
        ): StringRequest {
            return object : StringRequest(
                Method.POST,
                "http://$address:7000/post",
                response,
                error
            ) {
                override fun getParams(): MutableMap<String, String> {
                    return parameters
                }
            }
        }
    }

}