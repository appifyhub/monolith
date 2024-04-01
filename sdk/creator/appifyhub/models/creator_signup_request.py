# coding: utf-8

"""
    Appify Hub's Creator API

    The full specification of the service's API used by the project administrators.

    The version of the OpenAPI document: Latest
    Contact: contact@appifyhub.com
    Generated by OpenAPI Generator (https://openapi-generator.tech)

    Do not edit the class manually.
"""  # noqa: E501


from __future__ import annotations
import pprint
import re  # noqa: F401
import json

from datetime import date
from pydantic import BaseModel, ConfigDict, Field, StrictStr, field_validator
from typing import Any, ClassVar, Dict, List, Optional
from appifyhub.models.organization_dto import OrganizationDto
from typing import Optional, Set
from typing_extensions import Self

class CreatorSignupRequest(BaseModel):
    """
    A request to sign up a new creator
    """ # noqa: E501
    user_id: StrictStr = Field(description="The email of the creator")
    raw_signature: StrictStr = Field(description="The secret signature of the creator, usually a plain-text password")
    name: StrictStr = Field(description="The name of the creator")
    type: StrictStr = Field(description="The type of the creator")
    birthday: Optional[date] = Field(default=None, description="The birthday of the creator")
    company: Optional[OrganizationDto] = None
    signup_code: Optional[StrictStr] = Field(default=None, description="The signup code to use for sign up this creator (usually required by default)")
    __properties: ClassVar[List[str]] = ["user_id", "raw_signature", "name", "type", "birthday", "company", "signup_code"]

    @field_validator('type')
    def type_validate_enum(cls, value):
        """Validates the enum"""
        if value not in set(['PERSONAL', 'ORGANIZATION']):
            raise ValueError("must be one of enum values ('PERSONAL', 'ORGANIZATION')")
        return value

    model_config = ConfigDict(
        populate_by_name=True,
        validate_assignment=True,
        protected_namespaces=(),
    )


    def to_str(self) -> str:
        """Returns the string representation of the model using alias"""
        return pprint.pformat(self.model_dump(by_alias=True))

    def to_json(self) -> str:
        """Returns the JSON representation of the model using alias"""
        # TODO: pydantic v2: use .model_dump_json(by_alias=True, exclude_unset=True) instead
        return json.dumps(self.to_dict())

    @classmethod
    def from_json(cls, json_str: str) -> Optional[Self]:
        """Create an instance of CreatorSignupRequest from a JSON string"""
        return cls.from_dict(json.loads(json_str))

    def to_dict(self) -> Dict[str, Any]:
        """Return the dictionary representation of the model using alias.

        This has the following differences from calling pydantic's
        `self.model_dump(by_alias=True)`:

        * `None` is only added to the output dict for nullable fields that
          were set at model initialization. Other fields with value `None`
          are ignored.
        """
        excluded_fields: Set[str] = set([
        ])

        _dict = self.model_dump(
            by_alias=True,
            exclude=excluded_fields,
            exclude_none=True,
        )
        # override the default output from pydantic by calling `to_dict()` of company
        if self.company:
            _dict['company'] = self.company.to_dict()
        return _dict

    @classmethod
    def from_dict(cls, obj: Optional[Dict[str, Any]]) -> Optional[Self]:
        """Create an instance of CreatorSignupRequest from a dict"""
        if obj is None:
            return None

        if not isinstance(obj, dict):
            return cls.model_validate(obj)

        _obj = cls.model_validate({
            "user_id": obj.get("user_id"),
            "raw_signature": obj.get("raw_signature"),
            "name": obj.get("name"),
            "type": obj.get("type"),
            "birthday": obj.get("birthday"),
            "company": OrganizationDto.from_dict(obj["company"]) if obj.get("company") is not None else None,
            "signup_code": obj.get("signup_code")
        })
        return _obj


