import urllib.request
import re
req = urllib.request.Request('https://docs.papermc.io/misc/downloads-service/', headers={'User-Agent': 'Mozilla/5.0'})
try:
    html = urllib.request.urlopen(req).read().decode('utf-8')
    text = re.sub(r'<[^>]+>', ' ', html)
    text = re.sub(r'\s+', ' ', text)
    if '262' in text or '26.2' in text:
        print("Found in text:")
        for line in text.split('.'):
            if '262' in line or '26.2' in line:
                print(line.strip())
except Exception as e:
    print(e)
