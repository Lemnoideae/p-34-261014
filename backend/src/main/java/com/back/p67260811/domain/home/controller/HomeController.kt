package com.back.p67260811.domain.home.controller

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.net.InetAddress
import java.net.UnknownHostException

@RestController
class HomeController {
    @GetMapping(value = ["/index"], produces = [MediaType.TEXT_HTML_VALUE])
    @Throws(
        UnknownHostException::class
    )
    fun home(): String {
        val localhost = InetAddress.getLocalHost()

        return """
                <h1>Welcome to Rest1</h1>
                <p>Server IP Address: ${localhost.hostAddress}</p>
                <p>Server Host Name: ${localhost.hostName}</p>
                <div>
                    <a href="swagger-ui/index.html">API 문서로 이동</a>
                </div>
                
                """.trimIndent()
    }

    @GetMapping(value = ["test/fetchData"], produces = [MediaType.TEXT_HTML_VALUE])
    fun testFetch(): String {
        return """
                <script>
                    console.clear();
                    
                    fetch("http://localhost:8080/api/v1/posts")
                    .then(response => response.json())
                    .then(data => console.log(data))
                    
                    fetch("http://localhost:8080/api/v1/posts/1")
                    .then(response => response.json())
                    .then(data => console.log(data))
                    
                </script>
                
                """.trimIndent()
    }
}