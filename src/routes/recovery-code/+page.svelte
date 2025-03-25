<script lang="ts">
	import type { PageData } from "./$types";

	export let data: PageData;
</script>


<div class="flex h-screen place-content-center items-center">
	<div>
		<fieldset class="fieldset w-xs bg-base-200 border border-base-300 p-4 rounded-box">
			<legend class="fieldset-legend">Recovery code</legend>
		  
			<div>
				<span class="mb-2">Your recovery code is: </span>
				<span class="text-accent mb-2">{data.recoveryCode}</span>
			</div>
			<p class="text-info mb-2 text-pretty">You can use this recovery code if you lose access to your second factors.</p>
			
			<div class="flex items-center justify-end">
				<a href="/" class="p-1 link-hover">Next</a>
				<span>&DoubleLongRightArrow;</span>
			</div>
		</fieldset>
	</div>
</div>