# MessageTemplateCreateRequest

A request to create a message template

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**name** | **str** | The (unique) name of the message template | 
**language_tag** | **str** | The default language of the message template (locale represented as in IETF BCP 47) | 
**title** | **str** | The title of the message template | 
**content** | **str** | The template text content, including variables | 
**is_html** | **bool** | Whether the content is HTML or not | 

## Example

```python
from appifyhub.models.message_template_create_request import MessageTemplateCreateRequest

# TODO update the JSON string below
json = "{}"
# create an instance of MessageTemplateCreateRequest from a JSON string
message_template_create_request_instance = MessageTemplateCreateRequest.from_json(json)
# print the JSON string representation of the object
print(MessageTemplateCreateRequest.to_json())

# convert the object into a dict
message_template_create_request_dict = message_template_create_request_instance.to_dict()
# create an instance of MessageTemplateCreateRequest from a dict
message_template_create_request_form_dict = message_template_create_request.from_dict(message_template_create_request_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


