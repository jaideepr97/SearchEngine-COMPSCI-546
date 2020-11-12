#%%
import json
from collections import defaultdict
import sys

#%%
by_query = defaultdict(list)

file_name = 'extracted_features.jsonl'
if len(sys.argv) == 2:
    file_name = sys.argv[1]

with open(file_name) as fp:
    for line in fp:
        entry = json.loads(line)
        by_query[entry['qid']].append(entry)



#%%
import numpy as np
import sklearn
from sklearn.feature_extraction import DictVectorizer
from sklearn.model_selection import train_test_split, KFold
from sklearn.metrics import classification_report, roc_auc_score
from sklearn.ensemble import RandomForestClassifier
from sklearn.datasets import dump_svmlight_file

#%%
folds = KFold(n_splits=10, shuffle=True, random_state=42)

measures = defaultdict(list)
importances = defaultdict(list)
queries = np.array(sorted(by_query.keys()))
fold = 0
for train_qi, test_qi in folds.split(queries):
    train_q = queries[train_qi]
    test_q = queries[test_qi]

    train_data = []
    test_data = []
    train_ys = []
    test_ys = []
    train_qs = []
    test_qs = []

    for q in train_q:
        for entry in by_query[q]:
            train_qs.append(int(q))
            train_ys.append(entry['rel'])
            train_data.append(entry['features'])
    for q in test_q:
        for entry in by_query[q]:
            test_qs.append(int(q))
            test_ys.append(entry['rel'])
            test_data.append(entry['features'])
    
    feature_numbers = DictVectorizer()
    train_X = feature_numbers.fit_transform(train_data)
    test_X = feature_numbers.transform(test_data)

    fold += 1
    dump_svmlight_file(train_X.todense(), train_ys, f=('ltr/fold%02d.train' % fold), zero_based=False, query_id=train_qs)
    dump_svmlight_file(test_X.todense(), test_ys, f=('ltr/fold%02d.test' % fold), zero_based=False, query_id=test_qs)

    m = RandomForestClassifier(random_state=42, n_estimators=100)
    m.fit(train_X, np.array(train_ys) > 0)

    pred_y = m.predict(test_X)
    score_y = m.predict_proba(test_X)[:,1]
    measures['auc'].append(roc_auc_score(np.array(test_ys) > 0, score_y))
    for k, v in zip(feature_numbers.feature_names_, m.feature_importances_):
        importances[k].append(v)

print("Mean-AUC: %1.3f" % np.mean(measures['auc']))

for k in importances.keys():
    print(k, np.mean(importances[k]))
