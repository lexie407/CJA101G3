document.addEventListener("DOMContentLoaded", function(){
	let report_line = document.getElementsByClassName("report_line");
	for(let i = 0; i < report_line.length; i++){
		report_line[i].addEventListener("click", function(){
			let form = document.createElement("form");
			form.action = "/CommentsReports/memberReportListDetsil";
			form.method = "post";
			
			let input = document.createElement("input");
			input.type = "hidden";
			input.name = "commRepId";
			input.value = this.getAttribute("data-commRepId");
			form.appendChild(input);
			
			document.body.appendChild(form);
			form.submit();
		});
	}
});