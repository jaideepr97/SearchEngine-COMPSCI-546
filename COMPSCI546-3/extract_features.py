import gzip
import json
from trec_data import load_all_entity_topics, load_entity_judgments
import string
from bs4 import BeautifulSoup
from bs4.element import Comment

articles = {}
topics = load_all_entity_topics()
qrels = load_entity_judgments()

def extract_anchor_text(content):
    anchor_texts = []
    inside_anchors = False
    anchor_text = ""
    for i in range (len(content)):
        if content[i] == '>' and  not inside_anchors and content[i-1] != 'a':
            inside_anchors = True
            anchor_text = ""
            continue
        if content[i] == '<' and inside_anchors:
            inside_anchors = False
            anchor_texts.append(anchor_text)
            anchor_text = ""
        if inside_anchors:
            anchor_text += str(content[i])
    return anchor_texts 

def anchor_text_mentions(entity, article):
    num_anchor_texts = 0
    num_matches = 0
    # for every paragraph
    for para in article['contents']:
        if 'type' in para and para['type'] == "sanitized_html":
            if 'content' in para and type(para['content']) == str: 
                anchor_texts = extract_anchor_text(para['content'])
                num_anchor_texts += len(anchor_texts)
                for anchor_text in anchor_texts:
                    if entity in anchor_text:
                        num_matches += 1
    if num_anchor_texts == 0:
        return 0.0
    return num_matches / num_anchor_texts

def caption_mentions(entity, article):
    num_captions = 0
    num_matches = 0
    # for every paragraph
    for para in article['contents']:
        # that is a paragraph and has entity inside
        if 'fullcaption' in para and type(para['fullcaption']) == str and para['fullcaption'] != "": 
            num_captions += 1
            if entity in para['fullcaption']:
                num_matches += 1
        if 'images' in para and len(para['images']) > 0:
            for image in para['images']:
                if 'fullcaption' in image and type(image['fullcaption']) == str and image['fullcaption'] != "":
                    num_captions += 1
                    if entity in image['fullcaption']:
                        num_matches += 1
    if num_captions == 0:
        return 0.0
    return num_matches / num_captions


def title_mentions(entity, article):
    num_titles = 0
    num_matches = 0
    if 'title' in article and article['title'] != "" :
        num_titles += 1
        if entity in article['title']:
            num_matches += 1
        
    # for every paragraph
    for para in article['contents']:
        # that is a paragraph and has entity inside
        if 'title' in para and type(para['title']) == str and para['title'] != "": 
            num_titles += 1
            if entity in para['title']:
                num_matches += 1
        if 'images' in para and len(para['images']) > 0:
            for image in para['images']:
                if 'title' in image and type(image['title']) == str and image['title'] != "":
                    num_titles += 1
                    if entity in image['title']:
                        num_matches += 1
    return num_matches / num_titles

#%%
def paragraph_matches(entity, article):
    num_paras = len(article['contents'])
    num_matches = 0
    # for every paragraph
    for para in article['contents']:
        # that is a paragraph and has entity inside
        if 'content' in para and type(para['content']) == str and entity in para['content']:
            num_matches += 1
    return num_matches / num_paras


#%%
def extract_features(entity, article):
    f = {}
    mention = entity['mention']
    f['para-fraction'] = paragraph_matches(mention, article)
    f['title-fraction'] = title_mentions(mention, article)
    f['caption-fraction'] = caption_mentions(mention, article)
    f['anchor-text-fraction'] = anchor_text_mentions(mention, article)
    return f

def main():
    with open('extracted_features.jsonl', 'w') as out:
        articles = {}
        with open('data/docs.jsonl', 'rb') as fp:
            for line in fp:
                article = json.loads(line.decode('utf-8'))
                articles[article['id']] = article
        for topic in topics:
            docid = topic['docid']
            article = articles[docid]
            qid = topic['qid']
            for entity in topic['entities']:
                features = extract_features(entity, article)
            # ranksvm stuff as dict:
                keep = {}
                keep['qid'] = qid
                keep['features'] = features
                keep['docid'] = entity['id']
                keep['rel'] = qrels[qid].get(entity['id'], 0)
                print(json.dumps(keep), file=out)

if __name__ == '__main__':
  main()
