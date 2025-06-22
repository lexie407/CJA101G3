document.addEventListener("DOMContentLoaded", function(){
	let report_line = document.getElementsByClassName("noti-table-line");
	
	//每列案件添加click事件
	for(let i = 0; i < report_line.length; i++){
		report_line[i].addEventListener("click", function(){
			let form = document.createElement("form");
			form.action = "/report/editReport";
			form.method = "post";
			
			let input = document.createElement("input");
			input.name = "repId";
			input.value = this.getAttribute("data-repId");
			form.appendChild(input);
			
			document.body.appendChild(form);
			form.submit();
		});
	}
	
	//計算儀表數據
	let sta0 = 0;
	let sta1 = 0;
	for(let i = 0; i < report_line.length; i++){
		switch(report_line[i].getAttribute("data-repStatus")){
		case "0":
			sta0++;
			break;
		case "1":
			sta1++
			break;
		}
	}
	let staNum = document.getElementsByClassName("large-number");
	staNum[0].innerHTML = sta0;
	staNum[1].innerHTML = sta1;
	
	//datatable
	let table = new DataTable("#admin-table");
	
	//成功案件修改訊息
	let successMsg = document.getElementById("successMsg");
	if(successMsg.value != ""){
		alert(successMsg.value);	
	}
});