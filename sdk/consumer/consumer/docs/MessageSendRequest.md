# MessageSendRequest

A request to send a message. Must contain either a template ID or a template name.

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**message_type** | **str** | The type of the message to send | 
**message_template_id** | **int** | The ID of the message template to use | [optional] 
**message_template_name** | **str** | The (unique) name of the message template | [optional] 

## Example

```python
from appifyhub.models.message_send_request import MessageSendRequest

# TODO update the JSON string below
json = "{}"
# create an instance of MessageSendRequest from a JSON string
message_send_request_instance = MessageSendRequest.from_json(json)
# print the JSON string representation of the object
print(MessageSendRequest.to_json())

# convert the object into a dict
message_send_request_dict = message_send_request_instance.to_dict()
# create an instance of MessageSendRequest from a dict
message_send_request_form_dict = message_send_request.from_dict(message_send_request_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


