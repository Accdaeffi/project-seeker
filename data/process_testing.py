import threading

import process_events
import process_menu_items
import process_rumors

threads = [
    threading.Thread(target=process_events.process_file, args=['project-seeker-testing-data']),
    threading.Thread(target=process_menu_items.process_file, args=['project-seeker-testing-data']),
    threading.Thread(target=process_rumors.process_file, args=['project-seeker-testing-data'])
]

for i in threads:
    i.start()

for i in threads:
    i.join()
