# 유튜브 배치

### 목적

1. Youtube API를 사용하여 영상정보, 재생목록정보, 채널정보를 가져와서 DB에 저장합니다.
2. 저장된 데이터는 웹에서 검색 및 조회할때 사용됩니다.
3. 구글 감정분석을 이용하여 부정적인 댓글이 있는 영상이 있는 재생목록은 제외시켜서
    
    가능한한 양질의 영상들을 얻고자 합니다.
    

### 기능

1. DB에 저장된 키워드를 바탕으로 유튜브의 재생목록을 검색하여 재생목록에 있는 모든 영상들에 정보를 가져옵니다. 재생목록의 채널정보도 같이 가져옵니다.
2. 유튜브 API의 정해진 할당량 ( 10000 ) 안에서 API 호출이 가능하기 때문에 별도의 설정파일에서 검색 호출 횟수를 지정합니다.
3. 특정 채널에 있는 재생목록은 검색결과에서 제외시키고 DB에 저장 할 수 있습니다.
4. 감정분석을 적용할때 긍정수치는 설정파일에서 설정가능합니다.


- 감정분석 수치 참고 테이블

프로그램에 적용된 수치는 `"score"`  값만 사용하며 설정파일에 설정이 가능합니다.

| 감정 | 샘플 값 |
| --- | --- |
| 확실히 긍정적* | "score": 0.8, "magnitude": 3.0 |
| 확실히 부정적* | "score": -0.6, "magnitude": 4.0 |
| 중립적 | "score": 0.1, "magnitude": 0.0 |
| 혼합 | "score": 0.0, "magnitude": 4.0 |


- 설정파일 항목설정 : api-option.properties 파일

| 설정항목 이름 | 설명 | 비고 |
| --- | --- | --- |
| sentiment.threshold | 감정분석의 긍정수치입니다. 설정된 수치 이상의 긍정수치를 가진 영상들만 DB에 저장됩니다. 예) 0.1 →  score 수치가 0.1 | 감정분석 수치 참고 테이블 |
| sentiment.comment.count | 감정분석에 적용할 영상댓글의 갯수, 이 수치가 클수록 "magnitude" 값이 커지게 됩니다. 예) 50 → 50개의 댓글만 분석에 사용 |  |
| playlist.fetch.size | 유튜브 API로 한번에 가져올 재생목록의 수, 재생목록에 몇개의 영상이 있을지 알기 힘들기 때문에 10000 이라는 할당량을 고려해야 합니다. 예) 10 → 한번에 10개의 재생목록만 검색  | 키워드 1개당 할당량 100,영상의 정보는 할당량 1 |
| search.publish.date | 설정된 날짜 이후에 등록된 재생목록만 검색합니다. |  |
| exclusive.channels | 검색에는 포함되지만, DB에 넣기 위한 처리단계에서 배제할 채널의 ID |  |

### 사용기술

Spring batch 를 사용하여 구현하였습니다.
Java 버전 : 11

1 개의 Job, 1개의 Step, 1개의 Tasklet 으로 구성되어 있습니다.
유튜브 데이터를 수집하기 위한 목적으로 단순하게 구성화였습니다.
- 유튜브 Data v3 API, 구글 감정분석 API 를 사용하였습니다.

### ERD
DBMS : MySQL 8

![lilac-boot-erd](https://user-images.githubusercontent.com/113125088/220605834-08709c93-ab79-4faa-903c-27eeae9134ad.png)

