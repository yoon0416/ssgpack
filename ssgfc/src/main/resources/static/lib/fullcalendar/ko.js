/*!
FullCalendar v6.1.8
Docs & License: https://fullcalendar.io
(c) 2024 Adam Shaw
*/

FullCalendar.globalLocales.push(function () {
  'use strict';

  return {
    code: "ko",
    week: {
      dow: 0,
      doy: 1,
    },
    buttonText: {
      prev: "이전",
      next: "다음",
      today: "오늘",
      year: "연도",
      month: "월",
      week: "주",
      day: "일",
      list: "일정목록",
    },
    weekText: "주",
    allDayText: "종일",
    moreLinkText: function(n) {
      return "다른 일정 " + n + "개";
    },
    noEventsText: "일정이 없습니다",
  };
}());
