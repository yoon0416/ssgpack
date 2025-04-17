package com.ssgpack.ssgfc.crawler;

import org.jsoup.Jsoup;                          
import org.jsoup.nodes.Document;                
import org.jsoup.nodes.Element;                  
import org.jsoup.select.Elements;               

public class BackNum {                           
    public static void main(String[] args) {    
        try {
            
            String url = "https://statiz.sporki.com/team/?m=seasonBacknumber&t_code=9002&year=2025";

            // Jsoup을 사용하여 해당 URL의 HTML 문서 전체를 가져옴
            Document doc = Jsoup.connect(url).get();

            // 각 선수 블록(div.item.away)을 모두 선택 (등번호와 이름 포함된 div들)
            Elements items = doc.select("div.item.away"); 

            // 모든 선수 블록을 하나씩 반복
            for (Element item : items) {

                // 블록 내부에서 등번호(span.number)의 텍스트 추출
                String number = item.select("span.number").text();

                // 블록 내부에서 선수 이름(a 태그 내부)의 텍스트 추출
                String name = item.select("a").text();

                // 등번호와 이름이 모두 비어있지 않은 경우만 출력 이걸 출력이 아니라 db에 넣어야하는데
                if (!number.isEmpty() && !name.isEmpty()) {
                    System.out.println("등번호: " + number + " | 이름: " + name);
                }
            }

        } catch (Exception e) { e.printStackTrace(); }
        
    }//end psvm
}//end class
