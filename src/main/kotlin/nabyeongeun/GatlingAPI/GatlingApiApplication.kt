package nabyeongeun.gatlingAPI

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.core.io.ClassPathResource
import org.springframework.util.FileCopyUtils
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublisher
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

@SpringBootApplication
class GatlingApiApplication

fun main(args: Array<String>) {
//	runApplication<GatlingApiApplication>(*args)

	val lines = mutableListOf<String>()

	val resource = ClassPathResource("RequestList.txt")
	val inputStream = resource.inputStream
	val reader = BufferedReader(InputStreamReader(inputStream))

	reader.useLines { lines.addAll(it) }

	if(lines.isEmpty()) {
		print("RequestList is empty");
		return;
	}

	val client = HttpClient.newBuilder().build()
	val requestBuilder = HttpRequest.newBuilder()

	var presentLine = 1
	val totalLine = lines.size
	for(line in lines) {

		val fullURI = line.split(" ")

		val method = fullURI[0]
		val uri    = fullURI[1]

		if(!method.contains("GET") && !method.contains("POST")) {
			println("Unsupported http method : $line")
			break
		}

		val response = client.send(parameterParser(method, requestBuilder, uri), HttpResponse.BodyHandlers.ofString())
		println(presentLine.toString() + " of " + totalLine.toString() + " "+ response.uri())
		print(response.body())

		Thread.sleep(1000) // set delay

		presentLine += 1
	}
}

fun parameterParser(method : String, request : HttpRequest.Builder, uri: String) : HttpRequest {

	val paramMap = mutableMapOf<String,String>()

	val queryString = uri.split("?")
	val prefix = queryString[0]
	var postfix = "?"

	if(queryString.size > 1) {

		val params = queryString[1].split("&")

		for (param in params) {
			val keyValue = param.split("=")

			val key = keyValue[0]
			val value = URLEncoder.encode(keyValue[1], StandardCharsets.UTF_8)

			postfix += "$key=$value&"
		}
	}

	return request
		.uri(URI.create(prefix + postfix))
		.method(method, BodyPublishers.ofString(""))
		.header("Content-Type", "application/x-www-form-urlencoded")
		.build()
}
