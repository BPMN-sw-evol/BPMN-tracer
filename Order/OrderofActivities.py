# import xml.etree.ElementTree as ET

# def get_activity_names_in_order(bpmn_file):
#     tree = ET.parse(bpmn_file)
#     root = tree.getroot()

#     sequence_flows = root.findall('.//{http://www.omg.org/spec/BPMN/20100524/MODEL}sequenceFlow')
#     flow_mapping = {flow.attrib['sourceRef']: flow.attrib['targetRef'] for flow in sequence_flows}
#     reverse_flow_mapping = {v: k for k, v in flow_mapping.items()}

#     start_activities = [flow.attrib['sourceRef'] for flow in sequence_flows if flow.attrib['sourceRef'] not in flow_mapping.values()]

#     ordered_activities = []
#     visited_activities = set()

#     def traverse(current_activity):
#         if current_activity not in visited_activities:
#             visited_activities.add(current_activity)
#             ordered_activities.append(current_activity)
#             if current_activity in flow_mapping:
#                 next_activity = flow_mapping[current_activity]
#                 traverse(next_activity)

#     for activity in start_activities:
#         traverse(activity)

#     activity_names = []
#     for activity_id in (ordered_activities):
#         activity = root.find(f".//*[@id='{activity_id}']")
#         if activity is not None and 'name' in activity.attrib:
#             activity_names.append(activity.attrib['name'])

#     return activity_names

# bpmn_file_path = r'D:\Laboral\MSG-Foundation\MSGF-BPM-Engine\src\main\resources\MSGF-Test.bpmn'
# activities_order = get_activity_names_in_order(bpmn_file_path)
# print(activities_order)

import xml.etree.ElementTree as ET

def get_activity_names_in_order(bpmn_file):
    tree = ET.parse(bpmn_file)
    root = tree.getroot()

    sequence_flows = root.findall('.//{http://www.omg.org/spec/BPMN/20100524/MODEL}sequenceFlow')
    flow_mapping = {flow.attrib['sourceRef']: flow.attrib['targetRef'] for flow in sequence_flows}

    start_activities = [flow.attrib['sourceRef'] for flow in sequence_flows if flow.attrib['sourceRef'] not in flow_mapping.values()]

    ordered_activities = []
    visited_flows = set()

    for start_activity in start_activities:
        current_activity = start_activity
        while current_activity in flow_mapping and current_activity not in visited_flows:
            ordered_activities.append(current_activity)
            visited_flows.add(current_activity)
            current_activity = flow_mapping[current_activity]

        if current_activity not in ordered_activities:
            ordered_activities.append(current_activity)

    activity_names = []
    for activity_id in ordered_activities:
        activity = root.find(f".//*[@id='{activity_id}']")
        if activity is not None and 'name' in activity.attrib:
            activity_names.append(activity.attrib['name'])

    return activity_names

bpmn_file_path = r'MSGF-Test.bpmn'
activities_order = get_activity_names_in_order(bpmn_file_path)
print(activities_order)