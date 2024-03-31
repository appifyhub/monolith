# CreatorSignupRequest

A request to sign up a new creator

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**user_id** | **str** | The email of the creator | 
**raw_signature** | **str** | The secret signature of the creator, usually a plain-text password | 
**name** | **str** | The name of the creator | 
**type** | **str** | The type of the creator | 
**birthday** | **date** | The birthday of the creator | [optional] 
**company** | [**OrganizationDto**](OrganizationDto.md) |  | [optional] 
**signup_code** | **str** | The signup code to use for sign up this creator (usually required by default) | [optional] 

## Example

```python
from appifyhub.models.creator_signup_request import CreatorSignupRequest

# TODO update the JSON string below
json = "{}"
# create an instance of CreatorSignupRequest from a JSON string
creator_signup_request_instance = CreatorSignupRequest.from_json(json)
# print the JSON string representation of the object
print(CreatorSignupRequest.to_json())

# convert the object into a dict
creator_signup_request_dict = creator_signup_request_instance.to_dict()
# create an instance of CreatorSignupRequest from a dict
creator_signup_request_form_dict = creator_signup_request.from_dict(creator_signup_request_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


