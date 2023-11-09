import React, { useState } from "react";
import { Common } from "../../../../utils/global.styles";

import { NavLink, useParams } from "react-router-dom";

import Input from "@components/atoms/Input/Input";
import Button from "@components/atoms/Button/Button";
import { PiNotepad } from "react-icons/pi";

export default function HandOverDetailNurse() {
  const [inputs, setInputs] = useState([{ name: "간호일지 1", value: "" }]);
  const [showWarning, setShowWarning] = useState(false);
  const { patientId } = useParams();

  const addInput = () => {
    if (inputs[inputs.length - 1].value) {
      setInputs([
        ...inputs,
        { name: `간호일지 ${inputs.length + 1}`, value: "" },
      ]);
      setShowWarning(false);
    } else {
      setShowWarning(true);
    }
  };

  const handleInputChange = (e, index) => {
    const { value } = e.target;
    const newInputs = [...inputs];
    newInputs[index].value = value;
    setInputs(newInputs);
    if (value) setShowWarning(false);
  };

  return (
    <div
      style={{
        display: "flex",
        flexDirection: "column",
        margin: "0 auto",
        marginTop: "20px",
        gap: "10px",
        width: "calc(100% - 28px)",
      }}
    >
      <div style={{ display: "flex", justifyContent: "space-between" }}>
        <div style={{ display: "flex", flexDirection: "column" }}>
          <p
            style={{
              color: Common.color.black02,
              fontSize: Common.fontSize.fontM,
              fontWeight: Common.fontWeight.extrabold,
              marginTop: "10px",
            }}
          >
            간호일지 (Nursing log)
          </p>
          <p
            style={{
              lineHeight: "20px",
              fontSize: Common.fontSize.fontXS,
              marginTop: "10px",
            }}
          >
            작성된 간호일지에서 전달하고 싶은 내용을 가져오세요. <br />
            추가적인 코멘트 작성이 가능합니다.
          </p>
        </div>
        <div
          style={{
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
          }}
        >
          <NavLink
            to={"/patient/" + patientId + "/detail/journal"}
            onClick={() => {
              console.log(1);
              localStorage.setItem("preJournal", "handover");
            }}
          >
            <div
              style={{
                display: "flex",
                flexDirection: "column",
                alignItems: "center",
                background: Common.color.white01,
                padding: "10px 7px",
                borderRadius: "10px",
              }}
            >
              <div>
                <PiNotepad size={22} />
              </div>
              <span
                style={{
                  color: Common.color.black02,
                  fontSize: Common.fontSize.fontXS,
                  fontWeight: Common.fontWeight.bold,
                }}
              >
                간호일지
              </span>
            </div>
          </NavLink>
        </div>
      </div>
      <div
        style={{
          display: "flex",
          flexDirection: "column",
          width: "100%",
          gap: "10px",
          marginTop: "20px",
          color: Common.color.black02,
          fontSize: Common.fontSize.fontS,
          fontWeight: Common.fontWeight.bold,
          height: "490px",
          paddingBottom: "70px",
          overflowY: "auto",
        }}
      >
        {inputs.map((input, index) => (
          <React.Fragment key={index}>
            <p>▎{input.name}</p>
            <Input
              variant={"default"}
              value={input.value}
              onChange={(e) => handleInputChange(e, index)}
              props={"margin-bottom: 14px;"}
            />
          </React.Fragment>
        ))}
        <div style={{ height: "50px", width: "100%" }}>
          {showWarning && (
            <p
              style={{
                color: "red",
                fontSize: `${Common.fontSize.fontXXS}`,
                marginBottom: "15px",
                marginTop: "-16px",
              }}
            >
              내용을 입력해주세요.
            </p>
          )}
          <Button
            variant="primary"
            height="50px"
            width="100%"
            onClick={addInput}
          >
            추가
          </Button>
        </div>
      </div>
    </div>
  );
}
