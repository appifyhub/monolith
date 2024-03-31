# SignupCodeResponse

The response with a signup code

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**code** | **str** | The signup code to use for sign up this creator (usually required by default) | 
**is_used** | **bool** | Whether the code is used or not | 
**created_at** | **str** | The time the object was created (based on ISO 8601) | 
**used_at** | **str** | The time the code was used (based on ISO 8601) | [optional] 

## Example

```python
from appifyhub.models.signup_code_response import SignupCodeResponse

# TODO update the JSON string below
json = "{}"
# create an instance of SignupCodeResponse from a JSON string
signup_code_response_instance = SignupCodeResponse.from_json(json)
# print the JSON string representation of the object
print(SignupCodeResponse.to_json())

# convert the object into a dict
signup_code_response_dict = signup_code_response_instance.to_dict()
# create an instance of SignupCodeResponse from a dict
signup_code_response_form_dict = signup_code_response.from_dict(signup_code_response_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


