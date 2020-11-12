# trec-news-ent-ltr-starter
Shared repository for code, issues, ideas, notes, etc.

- Starting entities: [data/newsir18-entity-ranking-topics.xml](data/newsir18-entity-ranking-topics.xml)
- (qid, unused, entity-id, judgment) => [data/eqrels.txt](data/eqrels.txt)

#### requires Java 8, python3 and bash
## To set up python
## on OS/X use python3 and pip3
```
pip install -r requirements.txt
```

## To generate extracted_features.jsonl

Modify the ``extract_features(entity, article)`` method inside ``extract_features.py``; then:

```bash
python extract_features.py
```

## To convert to RankLib input files:

```bash
python ranking_test.py
```

Also predicts AUC using a RandomForestClassifier; the closest thing
sklearn gets to learning to rank.

## To run RankLib:

```bash
bash ltr/train.sh
```
