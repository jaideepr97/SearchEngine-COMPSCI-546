#%%
from bs4 import BeautifulSoup
import json
import urllib, os

#%%
from collections import defaultdict
def load_entity_judgments():
    judgments = defaultdict(defaultdict)
    with open('data/eqrels.txt') as fp:
        for line in fp:
            [qid, unused, eid, judgment] = line.strip().split()
            judgments[qid][eid] = int(judgment)
    return judgments

#%%
def parse_entity_topics_file(path):
    judgments = load_entity_judgments()

    with open(path) as fp:
        # link is magical in HTML and always self-closing so replace it
        raw_text = fp.read().replace('<link>',
                                     '<wiki>').replace('</link>', '</wiki>')
        soup = BeautifulSoup(raw_text, 'html.parser')

    topics_list = []

    for topic in soup.find_all('top'):
        qid = topic.find('num').string.split(':')[1].strip()
        url = topic.find('url').string.strip()
        docid = topic.find('docid').string.strip()
        topic_data = {'qid': qid, 'url': url, 'docid': docid, 'entities': []}

        for entity in topic.find_all('entity'):
            eid = entity.find('id').string.strip()
            mention = entity.find('mention').string.strip()
            link = entity.find('wiki').string.strip()
            ent_dict = {
                'id': eid,
                'mention': mention,
                'link': link,
            }
            if qid in judgments:
                ent_dict['judgment'] = judgments[qid][eid]
            topic_data['entities'].append(ent_dict)

        topics_list.append(topic_data)
    return topics_list


def load_entity_training_topics():
    return parse_entity_topics_file(
        os.path.join("data", "newsir18-entity-ranking-topics.xml"))


def load_entity_testing_topics():
    return parse_entity_topics_file(
        os.path.join("data", "newsir19-entity-ranking-topics.xml"))


def load_all_entity_topics():
    out = []
    out.extend(load_entity_training_topics())
    out.extend(load_entity_testing_topics())
    return out

#%%
if __name__ == '__main__':
    print(load_all_entity_topics()[0])
    # print(get_wiki_pages()[0])
