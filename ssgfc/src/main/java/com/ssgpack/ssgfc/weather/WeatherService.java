package com.ssgpack.ssgfc.weather;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WeatherService {

    @Value("${weather.api.key}")
    private String serviceKey;

    public Map<String, Object> getStadiumWeather(String stadium, int nx, int ny) {
        Map<String, Object> weatherMap = new LinkedHashMap<>();

        try {
            String baseDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String baseTime = LocalTime.now().minusMinutes(40).format(DateTimeFormatter.ofPattern("HHmm"));

            String urlStr = String.format(
                "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst" +
                "?serviceKey=%s&dataType=JSON&base_date=%s&base_time=%s&nx=%d&ny=%d",
                serviceKey, baseDate, baseTime, nx, ny
            );

            HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) sb.append(line);
            rd.close();

            JSONObject json = new JSONObject(sb.toString());
            JSONArray items = json.getJSONObject("response")
                                  .getJSONObject("body")
                                  .getJSONObject("items")
                                  .getJSONArray("item");

            weatherMap.put("stadium", stadium);

            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                String category = item.getString("category");
                String value = item.getString("obsrValue");

                switch (category) {
                    case "T1H":
                        weatherMap.put("temp", value + "℃");
                        break;
                    case "REH":
                        weatherMap.put("humidity", value + "%");
                        break;
                    case "WSD":
                        weatherMap.put("wind", value + "m/s");
                        break;
                    case "PTY":
                        String status = "";
                        String weathericon = "";   // lottiefiles.com
                        switch (value) {
                            case "0":
                                status = "맑음";
                                weathericon = "sunny_loop2.gif";
                                break;
                            case "1":
                                status = "비";
                                weathericon = "rain_loop2.gif";
                                break;
                            case "2":
                                status = "비/눈";
                                weathericon = "rain_snow_loop2.gif";
                                break;
                            case "3":
                                status = "눈";
                                weathericon = "snow_loop2.gif";
                                break;
                            default:
                                status = "정보 없음";
                                weathericon = "";
                                break;
                        }
                        weatherMap.put("weather", status);
                        weatherMap.put("weathericon", weathericon);
                        break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            weatherMap.put("error", "API 호출 실패");
        }

        return weatherMap;
    }
}
