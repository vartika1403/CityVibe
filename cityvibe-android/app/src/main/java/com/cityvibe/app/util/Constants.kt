package com.cityvibe.app.util

object Constants {

    // Hosted demo backend (Spring Boot API exposed via the Emergent ingress).
    // Works out-of-the-box on both the Android emulator and physical devices.
    const val BASE_URL = "http://192.168.1.3:8080/"

    // ---- To run against a LOCAL Spring Boot instance instead ----
    // Start the backend in /app/backend-springboot (port 8090), then use:
    //   Android Emulator : "http://10.0.2.2:8090/"
    //   Physical device  : "http://192.168.1.3:8090/"
    // For http (cleartext) URLs, network_security_config already permits
    // 10.0.2.2 and localhost.
}
