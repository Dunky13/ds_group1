import csv
from operator import itemgetter

''' this script edits the WoWSession session trace provided by The Game Trace Archvie as such that:

- Only a single PLAYER_LOGIN and PLAYER_LOGOUT event is stored of each unique player.

- Players that do not have BOTH a PLAYER_LOGIN event and a PLAYER_LOGIN event are removed.

- For each unique player, the earliest PLAYER_LOGIN event as per epoch timestamp will get the timestamp 0.
  - The PLAYER_LOGOUT event gets a timestamp which is relative in seconds to its PLAYER_LOGIN event.
  - The relative timestamp will be the difference in epoch with the first event and decreased by a factor x (because our game takes less time then a typical wow session).
  - Events are sorted on timestamp

'''

decreaseFactor = 5

with open ('WoWSession_Node_Player_Fixed_Dynamic.txt', newline='') as csvfile:
    traceReader = csv.reader(csvfile, delimiter=',')

    # this skips the header.
    next(traceReader, None)
    trace = (list(traceReader))


alreadyAdded = list()
relevantEvents = list()

for index, row in enumerate(trace):

    if row[3].strip() == 'PLAYER_LOGIN':

        if row[1].strip()+row[3].strip() not in alreadyAdded:
            loginEvent = row

            for r in trace[index:]:

                if r[3].strip() == 'PLAYER_LOGOUT' and r[1] == loginEvent[1]:
                    relevantEvents.append([loginEvent[1].strip(), 0.0, loginEvent[3].strip()])
                    relevantEvents.append([r[1].strip(), (float(r[2].strip()) - float(loginEvent[2].strip())) / decreaseFactor, r[3].strip()])
                    alreadyAdded.append(loginEvent[1].strip()+loginEvent[3].strip())

                    break


# minEpoch = float(min(x[1] for x in relevantEvents))
#
# for event in relevantEvents:
#     event[1] = (float(event[1]) - minEpoch) / decreaseFactor

relevantEventsSorted = sorted(relevantEvents, key=itemgetter(1))

with open('WoWSessionTraceSingleLoginLogout_simple.txt', 'w', newline='') as f:
    writer = csv.writer(f, delimiter=',')
    writer.writerow(['PlayerID', 'RelativeTime', 'Event'])
    writer.writerows(relevantEventsSorted)


