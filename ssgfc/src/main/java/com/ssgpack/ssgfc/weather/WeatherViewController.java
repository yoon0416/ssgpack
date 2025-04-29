package com.ssgpack.ssgfc.weather;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WeatherViewController {
	
	@GetMapping("/weather")
	public String weatherPage() {
		return "weather/weather";	
	}
}
