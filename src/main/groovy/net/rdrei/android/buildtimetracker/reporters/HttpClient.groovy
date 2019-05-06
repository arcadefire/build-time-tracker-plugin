package net.rdrei.android.buildtimetracker.reporters

import groovy.json.JsonBuilder

interface HttpClient {
    def openConnection(String urlString)
    def closeConnection()
    def send(Map<String, Object> data)
    String read()
}

class DefaultHttpClient implements HttpClient {

    HttpURLConnection connection
    OutputStreamWriter writer

    @Override
    def openConnection(String urlString) {
        def url = new URL(urlString)
        connection = url.openConnection() as HttpURLConnection
        connection.with {
            doInput = true
            doOutput = true
            requestMethod = "POST"
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        }
        writer = new OutputStreamWriter(connection.outputStream, "UTF-8")
    }

    @Override
    def send(Map<String, Object> data) {
        writer.write(new JsonBuilder(data).toString())
        writer.close()
    }

    @Override
    String read() {
        def bufferedReader = new BufferedReader(new InputStreamReader(connection.inputStream))
        def jsonString = new StringBuffer()
        String line

        while (bufferedReader.readLine() != null) {
            line = bufferedReader.readLine()
            jsonString.append(line)
        }

        bufferedReader.close()

        return jsonString
    }

    @Override
    def closeConnection() {
        connection.disconnect()
    }
}
