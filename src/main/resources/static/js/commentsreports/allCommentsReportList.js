document.addEventListener("DOMContentLoaded", function(){
	let report_line = document.getElementsByClassName("noti-table-line");
	
	//每列案件添加click事件
	for(let i = 0; i < report_line.length; i++){
		report_line[i].addEventListener("click", function(){
			let form = document.createElement("form");
			form.action = "/CommentsReports/editCommentsReportList";
			form.method = "post";
			
			let input = document.createElement("input");
			input.name = "commRepId";
			input.value = this.getAttribute("data-commRepId");
			form.appendChild(input);
			
			document.body.appendChild(form);
			form.submit();
		});
	}
	
	//計算儀表數據
	let sta0 = 0;
	let sta1 = 0;
	for(let i = 0; i < report_line.length; i++){
		if(report_line[i].getAttribute("data-repStatus") === 0){
			sta0++;
		}
	}
	let staNum = document.getElementsByClassName("large-number");
	staNum[0].innerHTML = sta0;
	
	//datatable
	let table = new DataTable("#admin-table");
	
	//成功案件修改訊息
	let successMsg = document.getElementById("successMsg");
	if(successMsg.value != ""){
		alert(successMsg.value);	
	}
});