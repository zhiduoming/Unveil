"""Extract text from .docx file and print to stdout."""
import zipfile
import xml.etree.ElementTree as ET
import sys
import io

def extract_text(docx_path):
    z = zipfile.ZipFile(docx_path)
    xml_content = z.read('word/document.xml')
    tree = ET.fromstring(xml_content)
    ns = 'http://schemas.openxmlformats.org/wordprocessingml/2006/main'
    paragraphs = []
    for p in tree.iter(f'{{{ns}}}p'):
        texts = []
        for t in p.iter(f'{{{ns}}}t'):
            if t.text:
                texts.append(t.text)
        if texts:
            paragraphs.append(''.join(texts))
    return '\n'.join(paragraphs)

if __name__ == '__main__':
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
    text = extract_text(sys.argv[1])
    print(text)
