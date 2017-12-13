import csv
from operator import itemgetter

''' this script edits the WoWSession session trace provided by the Game Trace Archive as such that:

- Only PLAYER_LOGIN and PLAYER_LOGOUT events are stored.

- The earliest event as per epoch timestamp will get the timestamp 0. 
  - All other events get a timestamp which is relative in seconds to this first event.
  - The relative timestamp will be the difference in epoch with the first event and decreased by a factor x (because our game takes less time then a typical wow session).
  - Events are sorted on timestamp

'''

decreaseFactor = 50

with open ('WoWSession_Node_Player_Fixed_Dynamic.txt', newline='') as csvfile:
    traceReader = csv.reader(csvfile, delimiter=',')

    # this skips the header
    next(traceReader, None)
    trace = (list(traceReader))

relevantEvents = list()
for row in trace:
    if row[3].strip() == 'PLAYER_LOGIN' or row[3].strip() == 'PLAYER_LOGOUT':
        relevantEvents.append([row[1].strip(), row[2].strip(), row[3].strip()])

minEpoch = float(min(x[1] for x in relevantEvents))

for event in relevantEvents:
    event[1] = (float(event[1]) - minEpoch) / decreaseFactor

relevantEventsSorted = sorted(relevantEvents, key=itemgetter(1))

with open('WoWSessionTraceEdited.txt', 'w', newline='') as f:
    writer = csv.writer(f, delimiter=',')
    writer.writerow(['PlayerID', 'RelativeTime', 'Event'])
    writer.writerows(relevantEventsSorted)

