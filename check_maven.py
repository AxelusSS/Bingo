import urllib.request
import xml.etree.ElementTree as ET

req = urllib.request.Request('https://repo.papermc.io/repository/maven-public/io/papermc/paper/paper-api/maven-metadata.xml', headers={'User-Agent': 'Mozilla/5.0'})
try:
    xml_data = urllib.request.urlopen(req).read()
    root = ET.fromstring(xml_data)
    versions = [v.text for v in root.find('versioning').find('versions').findall('version')]
    print(versions[-5:])
except Exception as e:
    print(e)
