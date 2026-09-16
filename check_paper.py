import urllib.request
from bs4 import BeautifulSoup
req = urllib.request.Request('https://docs.papermc.io/misc/downloads-service/', headers={'User-Agent': 'Mozilla/5.0'})
try:
    html = urllib.request.urlopen(req).read()
    soup = BeautifulSoup(html, 'html.parser')
    text = soup.get_text(separator=' ', strip=True)
    if '262' in text or '26.2' in text:
        print("Found in text:")
        for line in text.split('.'):
            if '262' in line or '26.2' in line:
                print(line.strip())
except Exception as e:
    print(e)
