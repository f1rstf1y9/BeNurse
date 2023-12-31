import React, { useEffect } from "react";
import { Link, useParams } from "react-router-dom";

import JournalDatePicker from "../../components/templates/Patient/JournalDatePicker";
import JournalTimeLine from "../../components/templates/Patient/JournalTimeLine";
import CreatePencilButton from "../../components/atoms/Button/CreatePencilButton";
import BottomSelectPanel from "../../components/templates/BottomSelectPanel/BottomSelectPanel";
import { useHandoverSetStore } from "../../store/store";

export default function PatientJournalMain() {
  const { patientId } = useParams();
  const { isFromHandOver } = useHandoverSetStore((state) => state);

  useEffect(() => {}, []);

  return (
    <div style={{ width: "100%", marginTop: "83px" }}>
      <JournalDatePicker />
      <JournalTimeLine patientId={patientId} />
      <Link
        to="write"
        style={{
          position: "absolute",
          right: "30px",
          bottom: "40px",
          zIndex: 1,
        }}
      >
        {isFromHandOver ? null : <CreatePencilButton />}
      </Link>
      <BottomSelectPanel
        modifyLabel={"일지 수정"}
        deleteLabel={"일지 삭제"}
      />
    </div>
  );
}
