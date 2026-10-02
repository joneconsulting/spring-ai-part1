# ch4 리뷰 분석 비교: POST /api/movies/review/jev vs /review/llm
# 실행: powershell -ExecutionPolicy Bypass -File .\review-compare.ps1
# 요구 사항: Windows 10(1803) 이상에 기본 포함된 curl.exe

$BaseUrl = "http://localhost:8080/api/movies/review"

$Reviews = @(
    "마지막 반전에서 범인이 사실 주인공의 형이었다는 게 밝혀질 때 소름이 돋았다. 두 시간 내내 긴장을 놓을 수 없었던 최고의 스릴러.",
    "웃기려고 애쓰는 게 너무 보여서 오히려 민망했다. 개그가 하나도 안 터지고 시간이 아까웠다.",
    "우주 정거장과 시간 여행 설정이 정말 치밀하다. 영상미도 압도적이라 IMAX로 한 번 더 볼 생각이다.",
    "두 사람이 결국 공항에서 헤어지고 10년 뒤 다시 만나는 결말은 뻔했지만 음악은 좋았다.",
    "액션도 있고 로맨스도 있고 웃긴 장면도 있는데 딱히 뭐 하나 기억에 남진 않는다.",
    "전형적인 범죄 느와르. 조직 보스의 몰락을 차갑게 그려 낸 수작이다."
)

# curl.exe 출력(UTF-8)을 한글 깨짐 없이 읽기 위해 콘솔 인코딩을 UTF-8로 설정
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

# 한글을 명령줄 인자로 넘기면 CP949로 바뀔 수 있으므로, BOM 없는 UTF-8 파일로 본문을 전달
$Utf8NoBom = New-Object System.Text.UTF8Encoding $false
$BodyFile = Join-Path $env:TEMP "review-compare-body.txt"

try {
    foreach ($r in $Reviews) {
        Write-Host ("=== " + $r.Substring(0, [Math]::Min(20, $r.Length)) + "...")
        [System.IO.File]::WriteAllText($BodyFile, $r, $Utf8NoBom)

        foreach ($m in @("jev", "llm")) {
            # PowerShell 5.1에서 curl은 Invoke-WebRequest의 별칭이므로 반드시 curl.exe로 호출
            $out = & curl.exe -s -w "  (HTTP %{http_code}, %{time_total}s)" `
                -X POST "$BaseUrl/$m" `
                -H "Content-Type: text/plain; charset=UTF-8" `
                --data-binary "@$BodyFile"
            Write-Host ("{0,-4} {1}" -f $m, ($out -join ""))
        }
    }
}
finally {
    Remove-Item $BodyFile -ErrorAction SilentlyContinue
}
